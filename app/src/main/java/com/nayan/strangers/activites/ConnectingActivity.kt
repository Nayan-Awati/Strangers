package com.nayan.strangers.activites

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.util.ObjectsCompat
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.nayan.strangers.R
import com.nayan.strangers.databinding.ActivityConnectingBinding
import com.nayan.strangers.models.User
import java.util.*
import kotlin.collections.HashMap

class ConnectingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConnectingBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private var isOkay = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConnectingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://strangervideocall-7b5e1-default-rtdb.asia-southeast1.firebasedatabase.app")
        val profile = intent.getStringExtra("profile")
        Glide.with(this@ConnectingActivity)
            .load(profile)
            .into(binding.imgProfileConnectivity)

        val username:String = auth.uid.toString()
        database.reference.child("users")
            .orderByChild("status").equalTo(0.0)
            .limitToFirst(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.childrenCount > 0){
                        //Room available
                        isOkay = true
                        for(childSnap in snapshot.children){
                            database.reference
                                .child("users")
                                .child(childSnap.key.toString())
                                .child("incoming")
                                .setValue(username)

                            database.reference
                                .child("users")
                                .child(childSnap.key.toString())
                                .child("status")
                                .setValue(1)

                            val intent = Intent(this@ConnectingActivity, CallActivity::class.java)
                            val incoming = childSnap.child("incoming").getValue<String>()
                            val createdBy = childSnap.child("createdBy").getValue<String>()
                            val isAvailable = childSnap.child("isAvailable").getValue<Boolean>()
                            intent.putExtra("username", username.toString())
                            intent.putExtra("incoming", incoming)
                            intent.putExtra("createdBy", createdBy)
                            intent.putExtra("isAvailable", isAvailable)
                            startActivity(intent)
                            finish()
                        }
                    }else{
                        //New Room created
                        val room = HashMap<String, Any>()
                        room.put("incoming", username)
                        room.put("createdBy", username)
                        room.put("isAvailable", true)
                        room.put("status", 0.0)

                        database.reference
                            .child("users")
                            .child(username)
                            .setValue(room).addOnSuccessListener {
                                database.reference
                                    .child("users")
                                    .child(username).addValueEventListener(object: ValueEventListener{
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if(snapshot.child("status").exists()){
                                                if(isOkay)
                                                    return
                                                isOkay = true
                                                if(snapshot.child("status").getValue<Double>() == 1.0){
                                                    val intent = Intent(this@ConnectingActivity, CallActivity::class.java)
                                                    val incoming = snapshot.child("incoming").getValue<String>()
                                                    val createdBy = snapshot.child("createdBy").getValue<String>()
                                                    val isAvailable = snapshot.child("isAvailable").getValue<Boolean>()
                                                    intent.putExtra("username", username)
                                                    intent.putExtra("incoming", incoming)
                                                    intent.putExtra("createdBy", createdBy)
                                                    intent.putExtra("isAvailable", isAvailable)
                                                    startActivity(intent)
                                                    finish()
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {

                                        }
                                    })
                            }

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }
}