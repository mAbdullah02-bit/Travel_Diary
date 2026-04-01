package com.example.traveldiary.Activities

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView

import com.example.traveldiary.R

class SignupActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val btnSignup = findViewById<Button>(R.id.signup_BTN)
        val signupAgree = findViewById<CheckBox>(R.id.signup_agree)
        val btnFB = findViewById<Button>(R.id.signup_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.signup_goBTN)
        val btnlogin = findViewById<TextView>(R.id.backtologin)
        val sEmail = findViewById<TextView>(R.id.signup_editmail)
        val sName = findViewById<TextView>(R.id.signup_name)
        val sPassC = findViewById<TextView>(R.id.signup_passC)
        val back = findViewById<ImageView>(R.id.signup_back)
        back.setOnClickListener {
            val intent=Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

        btnSignup.setOnClickListener {

            val usrEmail=sEmail.text.toString()
            val usrName=sName.text.toString()

            val intent=Intent(this, MainActivity::class.java)

            intent.putExtra("User Email",usrEmail)
            intent.putExtra("User Name",usrName)
            intent.putExtra("Is Guest", false)
            startActivity(intent)
            finish()
        }
        signupAgree.setOnClickListener {
            if (signupAgree.isChecked) {
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
        btnlogin.setOnClickListener {

            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnSignup.setOnClickListener {
            val intent= Intent (this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }

        fun verifyPassword( ): Boolean {
            var validPass : Boolean=false
            if (sPassC != sPassC){
                print("Psswords dont match")

            }
            else if (sPassC.length()<8){
                print("Weak Password")

            }
            else{
                validPass=true


            }
            return validPass

        }
    }



}