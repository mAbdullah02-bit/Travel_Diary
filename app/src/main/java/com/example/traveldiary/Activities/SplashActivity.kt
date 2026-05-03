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

        // 2. The Auto-Login Check (Requirement F1)
        // This ensures user sessions persist across app restarts.
        if (auth.currentUser != null) {
            navigateToMain(auth.currentUser?.email, false)
            return
        }

        // 3. Setup UI for new users or guests
        val btnGetStarted = findViewById<Button>(R.id.btn_splash)
        val btnGuest = findViewById<Button>(R.id.btn_guest)

        btnGetStarted.setOnClickListener {
            // Send to LoginActivity to handle Email/Password (F1 Requirement)
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnGuest.setOnClickListener {
            // Standard Guest login flow
            navigateToMain("Guest User", true)
        }
    }

    //function to centralize navigation to MainActivity.
    private fun navigateToMain(email: String?, isGuest: Boolean) {
        val intent = Intent(this, MainActivity::class.java)
        // Use consistent keys to avoid "null" errors in HomeFragment
        intent.putExtra("USER_EMAIL", email ?: "unknown")
        intent.putExtra("IS_GUEST", isGuest)
        startActivity(intent)
        finish() // Crucial: Removes Splash from the Backstack
    }
}