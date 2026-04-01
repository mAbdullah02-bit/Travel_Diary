package com.example.traveldiary.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.traveldiary.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val btnGetStarted = findViewById<Button>(R.id.btn_splash)
        val btnGuest = findViewById<Button>(R.id.btn_guest)

        btnGetStarted.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnGuest.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("USER_EMAIL", "Guest User")
            intent.putExtra("IS_GUEST", true)
            startActivity(intent)
            finish()


        }


    }


}