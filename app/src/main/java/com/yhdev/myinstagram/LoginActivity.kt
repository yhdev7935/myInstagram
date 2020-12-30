package com.yhdev.myinstagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        findViewById<android.widget.Button>(R.id.email_login_button).setOnClickListener {
            signinAndSignup()
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