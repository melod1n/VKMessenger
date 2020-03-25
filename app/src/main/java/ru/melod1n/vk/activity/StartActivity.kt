package ru.melod1n.vk.activity

import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_start.*
import ru.melod1n.vk.R
import ru.melod1n.vk.current.BaseActivity

class StartActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        enter.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java).apply { putExtra("open_login", "") })
        }
    }

}