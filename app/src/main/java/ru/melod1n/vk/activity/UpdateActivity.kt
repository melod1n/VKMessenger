package ru.melod1n.vk.activity

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import kotlinx.android.synthetic.main.activity_update.*
import org.json.JSONObject
import ru.melod1n.vk.BuildConfig
import ru.melod1n.vk.R
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.common.TaskManager
import ru.melod1n.vk.common.UpdateManager
import ru.melod1n.vk.current.BaseActivity
import ru.melod1n.vk.util.AndroidUtils
import ru.melod1n.vk.util.Util
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class UpdateActivity : BaseActivity(), UpdateManager.OnUpdateListener {

    companion object {
        private const val FILE_BASE_PATH = "file://"
        private const val MIME_TYPE = "application/vnd.android.package-archive"
        private const val PROVIDER_PATH = ".provider"
        private const val APP_INSTALL_PATH = "\"application/vnd.android.package-archive\""
    }


    private var isChecking = false
    private var isNewUpdate = false
    private var isDownloading = false

    private var downloadId = 0L

    private var lastCheckTime = ""

    private var newUpdate: UpdateManager.Update? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)

        val lastUpdate = AppGlobal.preferences.getString("lastUpdate", "")
        if (!lastUpdate.isNullOrEmpty()) {
            isNewUpdate = true
            newUpdate = UpdateManager.Update(JSONObject(lastUpdate))
        }

        UpdateManager.addOnUpdateListener(this)

        lastCheckTime = AppGlobal.preferences.getString("updateCheckTime", "") ?: ""

        refreshState()

        if (AndroidUtils.hasConnection())
            checkUpdates()

        updateCheckUpdates.setOnClickListener {
            lastCheckTime = "${System.currentTimeMillis()}"
            AppGlobal.preferences.edit().putString("updateCheckTime", lastCheckTime).apply()

            checkUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        UpdateManager.removeOnUpdateListener(this)
    }

    private fun installUpdate(file: File, destination: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val contentUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + PROVIDER_PATH, File(destination))

            val install = Intent(Intent.ACTION_VIEW)
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
            install.data = contentUri

            startActivity(install)
            // finish()
        } else {
            val install = Intent(Intent.ACTION_VIEW).also {
                it.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                it.setDataAndType(Uri.fromFile(file), MIME_TYPE)
            }

            startActivity(install)
            // finish()
        }
    }

    private fun downloadUpdate() {
        updateCheckUpdates.shrink()
        updateCheckUpdates.isClickable = false

        isDownloading = true
        refreshState()

        TaskManager.execute {
            val apkName = "${newUpdate!!.version} ${newUpdate!!.code}"

            val destination = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/$apkName.apk"

            val uri = Uri.parse("$FILE_BASE_PATH$destination")

            val file = File(destination)
            if (file.exists()) {
                file.delete()
            }

            val request = DownloadManager.Request(Uri.parse(newUpdate!!.downloadLink))

            request.setTitle("${getString(R.string.app_name)} ${apkName}.apk")
            request.setMimeType(MIME_TYPE)
            request.setDestinationUri(uri)
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(
                        context: Context,
                        intent: Intent
                ) {

                    installUpdate(file, destination)
                    context.unregisterReceiver(this)

                    runOnUiThread {
                        updateCheckUpdates.extend()
                        updateCheckUpdates.isClickable = true

                        isDownloading = false
                        refreshState()
                    }
                }
            }

            registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

            val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = manager.enqueue(request)
        }
    }

    private fun checkUpdates() {
        if (isChecking) return

        isChecking = true
        refreshState()

        UpdateManager.checkUpdates()
    }

    override fun onNewUpdate(response: String?, update: UpdateManager.Update?) {
        newUpdate = update
        isNewUpdate = update != null

        if (isNewUpdate) {
            AppGlobal.preferences.edit().putString("lastUpdate", response).apply()
        }

        isChecking = false
        runOnUiThread { refreshState() }
    }

    private fun refreshState() {
        when {
            isChecking -> {
                updateState.text = getString(R.string.update_state_checking)

                setAlpha(updateInfoLayout, true)
                setAlpha(updateProgress, false)
                setAlpha(updateCheckUpdates, true)
            }
            isDownloading -> {
                updateState.text = getString(R.string.update_state_downloading)

                setAlpha(updateInfoLayout, true)
                setAlpha(updateProgress, false)
                setAlpha(updateCheckUpdates, true)
            }
            else -> {
                if (isNewUpdate) {
                    updateCheckUpdates.text = getString(R.string.update_download)
                    updateCheckUpdates.icon = getDrawable(R.drawable.ic_file_download)
                } else {
                    updateCheckUpdates.text = getString(R.string.update_check_updates)
                    updateCheckUpdates.icon = getDrawable(R.drawable.ic_refresh)
                }

                updateCheckUpdates.setOnClickListener {
                    if (isNewUpdate) {
                        downloadUpdate()
                    } else {
                        checkUpdates()
                    }
                }

                updateState.text = getString(if (isNewUpdate) R.string.update_state_update_available else R.string.update_state_no_updates)

                updateVersion.text =
                        if (isNewUpdate)
                            getString(R.string.update_new_version, "${newUpdate!!.version} ${newUpdate!!.code}")
                        else getString(R.string.update_current_version, "${AppGlobal.appVersionName} ${AppGlobal.appVersionCode}")

                updateInfo.text =
                        when {
                            isNewUpdate -> getString(R.string.update_changelog, HtmlCompat.fromHtml(newUpdate!!.changelog, HtmlCompat.FROM_HTML_MODE_LEGACY))
                            lastCheckTime.isEmpty() -> ""
                            else -> getString(R.string.update_last_check_time, getCheckTime())
                        }

                setAlpha(updateInfoLayout, false)
                setAlpha(updateProgress, true)
                setAlpha(updateCheckUpdates, false)
            }
        }
    }

    private fun getCheckTime(): String {
        val time = lastCheckTime.toLong()

        val lastTime = Util.removeTime(Date(time))
        val currentTime = Util.removeTime(Date(System.currentTimeMillis()))

        val format = if (currentTime > lastTime) {
            "dd.MM.yyyy HH:mm"
        } else {
            "HH:mm"
        }

        return SimpleDateFormat(format, Locale.getDefault()).format(time)
    }

    private fun setAlpha(view: View, toZero: Boolean) {
        if (toZero) {
            view.animate().alpha(0F).setDuration(250).withEndAction { view.visibility = View.GONE }.start()
        } else {
            view.visibility = View.VISIBLE
            view.animate().alpha(1F).setDuration(250).start()
        }
    }

}