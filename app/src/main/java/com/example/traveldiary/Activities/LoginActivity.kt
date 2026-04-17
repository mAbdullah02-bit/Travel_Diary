package com.example.traveldiary.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var sharedPreferences: SharedPreferences

    // Google Catcher
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)

        //  AUTO start If already logged in
        if (auth.currentUser != null) {
            goToMainActivity(auth.currentUser?.email ?: "User")
            return
        }

        val btnLogin = findViewById<Button>(R.id.login_BTN)
        val loginCheck = findViewById<CheckBox>(R.id.login_remember)
        val btnFB = findViewById<Button>(R.id.login_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.login_goBTN)
        val btnForgot = findViewById<TextView>(R.id.login_forgot)
        val btnSignup = findViewById<TextView>(R.id.login_signup)
        val semail = findViewById<TextView>(R.id.login_editemail)
        val spass = findViewById<TextView>(R.id.login_editpass)

        val savedEmail = sharedPreferences.getString("EMAIL", "")
        val savedPass = sharedPreferences.getString("PASSWORD", "")
        if (savedEmail!!.isNotEmpty()) {
            semail.text = savedEmail
            spass.text = savedPass
            loginCheck.isChecked = true
        }

        //  GOOGLE SETUP
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("YOUR_WEB_CLIENT_ID_HERE") // Keep your working Web Client ID here!
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //  FACEBOOK SETUP
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }
            override fun onCancel() {
                Toast.makeText(this@LoginActivity, "Facebook Login Canceled", Toast.LENGTH_SHORT).show()
            }
            override fun onError(error: FacebookException) {
                Toast.makeText(this@LoginActivity, "Facebook Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        //  BUTTON CLICKS

        btnLogin.setOnClickListener {
            val usrEmail = semail.text.toString()
            val usrPass = spass.text.toString()


            if (loginCheck.isChecked) {
                sharedPreferences.edit().putString("EMAIL", usrEmail).putString("PASSWORD", usrPass).apply()
            } else {
                sharedPreferences.edit().clear().apply()
            }

            goToMainActivity(usrEmail)
        }

        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        btnFB.setOnClickListener {
            // Launch the official Facebook login popup
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
        }

        btnForgot.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
            finish()
        }

        btnSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }
    }

    // --- FACEBOOK CATCHER: This is required to catch the result from the FB popup ---
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // --- FIREBASE AUTHENTICATION FUNCTIONS ---

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goToMainActivity(auth.currentUser?.email ?: "Google User")
                else Toast.makeText(this, "Firebase Google Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) goToMainActivity(auth.currentUser?.email ?: "Facebook User")
                else Toast.makeText(this, "Firebase FB Auth Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goToMainActivity(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User Email", email)
        intent.putExtra("Is Guest", false)
        startActivity(intent)
        finish()
    }
}