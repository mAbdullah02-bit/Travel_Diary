package com.example.traveldiary.Activities

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import com.example.traveldiary.R

class LoginActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.login_BTN)
        val loginCheck = findViewById<CheckBox>(R.id.login_remember)
        val btnFB = findViewById<Button>(R.id.login_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.login_goBTN)
        val btnForgot = findViewById<TextView>(R.id.login_forgot)
        val btnSignup = findViewById<TextView>(R.id.login_signup)
        val semail = findViewById<TextView>(R.id.login_editemail)
        val spass = findViewById<TextView>(R.id.login_editpass)

         btnLogin.setOnClickListener {

             val usrEmail=semail.text.toString()
             val intent=Intent(this, MainActivity::class.java)

             intent.putExtra("User Email",usrEmail)
             intent.putExtra("Is Guest", false)
             startActivity(intent)
             finish()
         }
        loginCheck.setOnClickListener {
            if (loginCheck.isChecked) {
            //    semail.isEnabled = true   add func for remember me
              //  spass.isEnabled = true
            } else {
               // semail.isEnabled = false
               // spass.isEnabled = false
            }

        }
        btnFB.setOnClickListener {
            // Login with facebook account
        }
        btnGoogle.setOnClickListener {
            // Login with google account
        }
        btnForgot.setOnClickListener {

            val intent= Intent(this, ForgotActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSignup.setOnClickListener {
val intent= Intent (this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }


    }



}