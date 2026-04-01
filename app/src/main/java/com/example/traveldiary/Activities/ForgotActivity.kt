package com.example.traveldiary.Activities

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.example.traveldiary.R

class ForgotActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)

        val btnreset = findViewById<Button>(R.id.forgot_BTN)
        val btnBack = findViewById<TextView>(R.id.forgot_backBTN)
        val btnBack2 = findViewById<ImageView>(R.id.forgot_back)
        val semail = findViewById<TextView>(R.id.forgot_editmail)


        btnreset.setOnClickListener {

         // RESET PAssword Instructions logic here


            val email= semail.text.toString()
            val resetCode =123456

            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


        btnBack.setOnClickListener {

            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnBack2.setOnClickListener {
            val intent= Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }


    }



}