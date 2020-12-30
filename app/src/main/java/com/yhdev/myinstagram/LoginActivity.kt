package com.yhdev.myinstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        findViewById<android.widget.Button>(R.id.email_login_button).setOnClickListener {
            signinAndSignup()
        }
        findViewById<android.widget.Button>(R.id.google_sign_in_button).setOnClickListener {
            // First Step
            googleLogin()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == GOOGLE_LOGIN_CODE) {
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result!!.isSuccess) {
                var account = result.signInAccount
                // second step
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {
                    task ->
                if(task.isSuccessful) {
                    // Creating a user account
                    moveMainPage(task.result?.user)
                }
                else if(!task.exception?.message.isNullOrEmpty()) {
                    // show Error Message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
                else {
                    // Login if you have account
                    signinEmail()
                }
            }
    }
    fun getEmailAddress(): String {
        return findViewById<EditText>(R.id.email_edittext).text.toString()
    }

    fun getPassword(): String {
        return findViewById<EditText>(R.id.password_edittext).text.toString()
    }
    fun signinAndSignup() {
        auth?.createUserWithEmailAndPassword(getEmailAddress(), getPassword())
            ?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful) {
                        // Creating a user account
                        moveMainPage(task.result?.user)
                    }
                    else if(!task.exception?.message.isNullOrEmpty()) {
                        // show Error Message
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                    else {
                        // Login if you have account
                        signinEmail()
                    }
        }
    }

    fun signinEmail() {
        auth?.signInWithEmailAndPassword(getEmailAddress(), getPassword())
            ?.addOnCompleteListener {
                task ->
                    if(task.isSuccessful) {
                        // Login
                        moveMainPage(task.result?.user)
                    }
                    else {
                        // show the Error Message
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if(user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

}