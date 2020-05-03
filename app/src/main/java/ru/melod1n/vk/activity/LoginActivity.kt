package ru.melod1n.vk.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.toolbar.*
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.api.VKAuth
import ru.melod1n.vk.common.AppGlobal
import ru.melod1n.vk.current.BaseActivity

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        prepareToolbar()
        prepareRefreshLayout()

        prepareSettings()

        val url = VKAuth.getUrl(UserConfig.API_ID, VKAuth.settings)
        webView.loadUrl(url)
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun prepareSettings() {
        webView.settings.javaScriptEnabled = true
        webView.clearCache(true)
        webView.webViewClient = VKWebClient()

        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
    }

    private fun prepareToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        toolbar.navigationIcon?.setTint(AppGlobal.colorAccent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun prepareRefreshLayout() {
        swipeRefreshLayout.apply {
            setColorSchemeColors(AppGlobal.colorAccent)
            setOnRefreshListener {
                webView!!.reload()
                isRefreshing = false
            }
        }
    }

    private inner class VKWebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)

            progressBar.visibility = View.VISIBLE

            view.visibility = View.GONE
            parseUrl(url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)

            progressBar.visibility = View.GONE
            view.visibility = View.VISIBLE
        }
    }

    private fun parseUrl(url: String) {
        try {
            if (TextUtils.isEmpty(url)) return
            if (url.startsWith(VKAuth.redirect_url)) {
                if (!url.contains("error=")) {
                    val auth = VKAuth.parseRedirectUrl(url)
                    val token = auth[0]
                    val id = auth[1].toInt()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra("user_id", id)

                    finishAffinity()
                    startActivity(intent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}