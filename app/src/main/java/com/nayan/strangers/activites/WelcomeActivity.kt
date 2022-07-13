package com.nayan.strangers.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.nayan.strangers.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<Button>(R.id.getStarted).setOnClickListener {
            startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
            finish()
        }
    }
}