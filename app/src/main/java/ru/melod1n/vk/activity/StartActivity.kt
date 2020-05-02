package ru.melod1n.vk.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import kotlinx.android.synthetic.main.activity_start.*
import ru.melod1n.vk.R
import ru.melod1n.vk.api.UserConfig
import ru.melod1n.vk.current.BaseActivity

class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        startEnter.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        startEnter.setOnLongClickListener {
            AlertDialog.Builder(this).apply {
                setTitle(R.string.custom_data)

                val view = LayoutInflater.from(this@StartActivity).inflate(R.layout.activity_login_custom_data, null, false) as View
                setView(view)

                val userId = view.findViewById<AppCompatEditText>(R.id.customDataUserId)
                val token = view.findViewById<AppCompatEditText>(R.id.customDataToken)

                setPositiveButton(android.R.string.ok) { _, _ ->
                    if (userId.text.toString().isEmpty() || token.text.toString().isEmpty()) return@setPositiveButton
                    val id = userId.text.toString().toInt()
                    val accessToken = token.text.toString()

                    if (id < 1) return@setPositiveButton

                    UserConfig.apply {
                        this.userId = id
                        this.token = accessToken
                    }.save()

                    finish()
                    startActivity(Intent(this@StartActivity, MainActivity::class.java))
                }

                setCancelable(false)
                setNegativeButton(android.R.string.cancel, null)
            }.show()
            true
        }

        startLoginSettings.setOnClickListener {
            Toast.makeText(this, R.string.in_progress_placeholder, Toast.LENGTH_LONG).show()
        }
    }

}