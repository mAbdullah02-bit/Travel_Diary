package com.example.traveldiary.Activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.traveldiary.R
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager

    // Google Catcher
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-Up Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()

        // Find all UI Elements
        val btnSignup = findViewById<Button>(R.id.signup_BTN)
        val signupAgree = findViewById<CheckBox>(R.id.signup_agree)
        val btnFB = findViewById<Button>(R.id.signup_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.signup_goBTN)
        val btnlogin = findViewById<TextView>(R.id.backtologin)
        val sEmail = findViewById<TextView>(R.id.signup_editmail)
        val sName = findViewById<TextView>(R.id.signup_name)
        val sPassC = findViewById<TextView>(R.id.signup_passC)
        val back = findViewById<ImageView>(R.id.signup_back)

        // IMPORTANT: I added this line! Ensure R.id.signup_pass matches your XML!
        val sPass = findViewById<TextView>(R.id.signup_pass)

        // --- GOOGLE SETUP ---
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID_HERE") // Use your exact same Web Client ID from LoginActivity!
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // --- FACEBOOK SETUP ---
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }
            override fun onCancel() {
                Toast.makeText(this@SignupActivity, "Facebook Sign-Up Canceled", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(this@SignupActivity, "Facebook Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // --- NAVIGATION CLICKS ---
        back.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnlogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // --- SOCIAL BUTTON CLICKS ---
        btnGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        btnFB.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        }

        // --- MANUAL EMAIL & PASSWORD SIGNUP ---
        btnSignup.setOnClickListener {
            val usrEmail = sEmail.text.toString().trim()
            val usrName = sName.text.toString().trim()
            val usrPass = sPass.text.toString()
            val usrPassC = sPassC.text.toString()

            // 1. Check if they agreed to the Checkbox
            if (!signupAgree.isChecked) {
                Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Stops the code here
            }

            // 2. Make sure boxes aren't empty
            if (usrEmail.isEmpty() || usrName.isEmpty() || usrPass.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 3. Verify Passwords match and are strong
            if (usrPass != usrPassC) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usrPass.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. Tell Firebase to create the account!
            auth.createUserWithEmailAndPassword(usrEmail, usrPass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                        goToMainActivity(usrEmail, usrName)
                    } else {
                        // If email already exists or is badly formatted, Firebase tells the user here
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    // --- FACEBOOK CATCHER ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // --- FIREBASE BRIDGES ---
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goToMainActivity(auth.currentUser?.email ?: "Google User", "Google User")
                else Toast.makeText(this, "Firebase Google Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goToMainActivity(auth.currentUser?.email ?: "Facebook User", "Facebook User")
                else Toast.makeText(this, "Firebase FB Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    // --- NAVIGATION LOGIC ---
    private fun goToMainActivity(email: String, name: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User Email", email)
        intent.putExtra("User Name", name)
        intent.putExtra("Is Guest", false)
        startActivity(intent)
        finish()
    }
}