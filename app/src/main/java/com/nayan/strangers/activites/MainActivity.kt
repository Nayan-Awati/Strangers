package com.nayan.strangers.activites

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.kaopiz.kprogresshud.KProgressHUD
import com.nayan.strangers.R
import com.nayan.strangers.databinding.ActivityCallBinding
import com.nayan.strangers.databinding.ActivityMainBinding
import com.nayan.strangers.models.User
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var user:User
    private var coins: Long = 0
    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    var requestCode = 1;
    private lateinit var progress: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = KProgressHUD.create(this@MainActivity)
        progress.setDimAmount(0.5f)
        progress.show()


        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://strangervideocall-7b5e1-default-rtdb.asia-southeast1.firebasedatabase.app")
        val currentUser = auth.currentUser

        database.reference.child("profiles")
            .child(currentUser?.uid.toString())
            .addValueEventListener( object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progress.dismiss()
                    user = snapshot.getValue<User>()!!
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
            if(isPermissionGranted()) {
                if (coins < 5) {
                    Toast.makeText(this@MainActivity, "Insufficient Balance", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    coins = coins-5
                    database.reference.child("profiles")
                        .child(currentUser?.uid.toString())
                        .child("coins")
                        .setValue(coins)
                    val intent = Intent(this@MainActivity, ConnectingActivity::class.java)
                    intent.putExtra("profile", user?.profile.toString())
                    startActivity(intent)
                }
            }else{
                askPermissions()
            }
        }



    }


    fun askPermissions(){
        ActivityCompat.requestPermissions(this@MainActivity, permissions, requestCode)
    }

    fun isPermissionGranted():Boolean{
        for( permission in permissions){
            if(ActivityCompat.checkSelfPermission(this@MainActivity, permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }


        return true;
    }

}