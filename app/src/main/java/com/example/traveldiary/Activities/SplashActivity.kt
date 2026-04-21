package com.example.traveldiary.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.traveldiary.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 1. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 2. The Auto-Login Check!
        // If a user is already logged in, skip this screen entirely.
        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("User Email", auth.currentUser?.email)
            intent.putExtra("Is Guest", false)
            startActivity(intent)
            finish() // Destroys the splash screen
            return   // Stops the rest of the code in this function from running
        }

        // 3. If no user is logged in, load the buttons as normal
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