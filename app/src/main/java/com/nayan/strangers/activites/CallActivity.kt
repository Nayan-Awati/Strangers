package com.nayan.strangers.activites

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.nayan.strangers.R
import com.nayan.strangers.databinding.ActivityCallBinding
import com.nayan.strangers.databinding.ActivityConnectingBinding
import com.nayan.strangers.models.InterfaceKotlin
import com.nayan.strangers.models.User
import java.util.*

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private lateinit var auth:FirebaseAuth
    private var uniqueId: String = ""
    private var username = ""
    private var friendUsername = ""
    private var isPeerConnected = false
    private var isAudio = true
    private var isVideo = true
    private var pageExit = false
    private lateinit var firebaseRef: DatabaseReference
    private lateinit var createdBy : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseRef = FirebaseDatabase.getInstance("https://strangervideocall-7b5e1-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users")
        username = intent.getStringExtra("username").toString()
        createdBy = intent.getStringExtra("createdBy").toString()
        var incoming = intent.getStringExtra("incoming").toString()
//        friendUsername = ""
//        if(incoming.equals(friendUsername, ignoreCase = true))
//            friendUsername = incoming
        friendUsername = incoming
        setupWebView()

        binding.btnMute.setOnClickListener {
            isAudio = !isAudio
            callJsFunction("javascript:toggleAudio(\"$isAudio\")")
            if(isAudio){
                binding.btnMute.setImageResource(R.drawable.btn_unmute_normal)
            }else{
                binding.btnMute.setImageResource(R.drawable.btn_mute_normal)
            }
        }
        binding.btnVideo.setOnClickListener {
            isVideo = !isVideo
            callJsFunction("javascript:toggleVideo(\"$isVideo\")")
            if(isVideo){
                binding.btnVideo.setImageResource(R.drawable.btn_video_normal)
            }else{
                binding.btnVideo.setImageResource(R.drawable.btn_video_muted)
            }
        }

        binding.btnEndCall.setOnClickListener {
            onDestroy()
        }

    }

    @SuppressLint("JavascriptInterface")
    fun setupWebView(){
        binding.webView.webChromeClient = object: WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                super.onPermissionRequest(request)
                request?.grant(request.resources)
            }
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.addJavascriptInterface(InterfaceKotlin(this@CallActivity)::class.java, "Android")

        //loadVideoCall
        loadVideoCall()

    }

    public fun loadVideoCall(){
        val filePath = "file:android_assets/call.html"
        binding.webView.loadUrl(filePath)
        binding.webView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                //initalize Peer
                initializePeer()


            }
        }

    }

    private fun initializePeer(){
        uniqueId = getUniqueId()
        callJsFunction("javascript:init(\"$uniqueId\")")
        if(createdBy.equals(username, ignoreCase = true)){
            if(pageExit)
                return
            firebaseRef.child(username).child("connId").setValue(uniqueId)
            firebaseRef.child(username).child("isAvailable").setValue(true)
            binding.loadingGroup.visibility = View.GONE
            binding.controls.visibility = View.VISIBLE

            FirebaseDatabase.getInstance().reference
                .child("profiles")
                .child(friendUsername)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val user = snapshot.getValue<User>()
                        Glide.with(this@CallActivity)
                            .load(user?.profile.toString())
                            .into(binding.profile)

                        binding.name.setText(user?.name.toString())
                        binding.city.setText(user?.city.toString())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })

        }else{
            Handler().postDelayed({
                kotlin.run {
                    friendUsername = createdBy
                    FirebaseDatabase.getInstance().reference
                        .child("profiles")
                        .child(friendUsername)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val user = snapshot.getValue<User>()
                                Glide.with(this@CallActivity)
                                    .load(user?.profile.toString())
                                    .into(binding.profile)

                                binding.name.setText(user?.name.toString())
                                binding.city.setText(user?.city.toString())
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    FirebaseDatabase.getInstance().reference
                        .child("users")
                        .child(friendUsername)
                        .child("connId")
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if(snapshot.value != null){
                                    //sendCallRequest
                                    sendCallRequest()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                }
            },3000)
        }

    }
    public fun onPeerConneted(){
        isPeerConnected = true
    }

    private fun sendCallRequest(){
        if(!isPeerConnected){
            Toast.makeText(this@CallActivity, "You are not Connected. Please check your connection", Toast.LENGTH_SHORT )
                .show()
            return
        }
        listenConnId()
    }

    private fun listenConnId() {
        firebaseRef.child(friendUsername).child("connId")
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.value == null)
                        return
                    binding.loadingGroup.visibility = View.GONE
                    binding.controls.visibility = View.VISIBLE
                    var connId = snapshot.getValue<String>()
                    callJsFunction("javascript:startCall(\"$connId\")")


                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun callJsFunction(function: String){
        binding.webView.post {
            kotlin.run {
                binding.webView.evaluateJavascript(function, null)
            }
        }
    }


    private fun getUniqueId():String{
        return UUID.randomUUID().toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        pageExit = true
        firebaseRef.child(createdBy).setValue(null)
        finish()
    }
}