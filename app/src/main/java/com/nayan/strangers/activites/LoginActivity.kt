package com.nayan.strangers.activites

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.nayan.strangers.R
import com.nayan.strangers.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseFb: FirebaseDatabase

    companion object{
        private const val RC_SIGN_IN = 11
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        databaseFb = FirebaseDatabase.getInstance("https://strangervideocall-7b5e1-default-rtdb.asia-southeast1.firebasedatabase.app")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)




        findViewById<LinearLayout>(R.id.btn_login).setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode ==  RC_SIGN_IN){
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.result
            authWithGoogle(account.idToken.toString())

        }
    }

    private fun authWithGoogle(idToken: String){
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if(it.isSuccessful){
                    val user: FirebaseUser? = auth.currentUser
                    val firebaseUser = User(user?.uid.toString(), user?.displayName.toString(), user?.photoUrl.toString(), "-", 500)
                    databaseFb.reference
                        .child("profiles")
                        .child(user?.uid.toString())
                        .setValue(firebaseUser)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                finishAffinity()
                            }else{
                                Toast.makeText(this, it.exception?.localizedMessage.toString(), Toast.LENGTH_SHORT).show()
                            }
                        }
                    Log.e("profile", user?.photoUrl.toString())
                }else{
                    Log.e("error", it.exception.toString())
                }
            }
    }



}