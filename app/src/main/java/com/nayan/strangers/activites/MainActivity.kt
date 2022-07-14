package com.nayan.strangers.activites

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.nayan.strangers.R
import com.nayan.strangers.databinding.ActivityCallBinding
import com.nayan.strangers.databinding.ActivityMainBinding
import com.nayan.strangers.models.User


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var coins: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://strangervideocall-7b5e1-default-rtdb.asia-southeast1.firebasedatabase.app")
        val currentUser = auth.currentUser

        database.reference.child("profiles")
            .child(currentUser?.uid.toString())
            .addValueEventListener( object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue<User>()
                    if (user != null) {
                        coins = user.coins
                    }
                    binding.txtCoins.setText("You have $coins")
                    Glide.with(this@MainActivity)
                        .load(user?.profile)
                        .into(binding.imgProfile)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                    Log.w(TAG, "loadPost:onCancelled", error.toException())
                }
            })

        binding.btnFind.setOnClickListener {
            if(coins < 50){
                startActivity(Intent(this@MainActivity, ConnectingActivity::class.java))
            }else{
                Toast.makeText(this@MainActivity, "Call Connecting...", Toast.LENGTH_SHORT).show()
            }
        }



    }
}