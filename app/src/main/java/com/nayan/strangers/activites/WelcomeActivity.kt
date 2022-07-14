package com.nayan.strangers.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.nayan.strangers.R

class WelcomeActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!= null){
            moveToNextActivity()
        }
        findViewById<Button>(R.id.getStarted).setOnClickListener {
            moveToNextActivity()
        }
    }


    private fun moveToNextActivity(){
        startActivity(Intent(this@WelcomeActivity, LoginActivity::class.java))
        finish()
    }
}