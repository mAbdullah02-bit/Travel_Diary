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
import com.example.traveldiary.DatabaseHelper
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
    private lateinit var dbHelper: DatabaseHelper // NEW: Local DB

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

        auth = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)

        val btnSignup = findViewById<Button>(R.id.signup_BTN)
        val signupAgree = findViewById<CheckBox>(R.id.signup_agree)
        val btnFB = findViewById<Button>(R.id.signup_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.signup_goBTN)
        val btnlogin = findViewById<TextView>(R.id.backtologin)
        val sEmail = findViewById<TextView>(R.id.signup_editmail)
        val sName = findViewById<TextView>(R.id.signup_name)
        val sPassC = findViewById<TextView>(R.id.signup_passC)
        val back = findViewById<ImageView>(R.id.signup_back)
        val sPass = findViewById<TextView>(R.id.signup_pass)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) { handleFacebookAccessToken(result.accessToken) }
            override fun onCancel() { Toast.makeText(this@SignupActivity, "Facebook Sign-Up Canceled", Toast.LENGTH_SHORT).show() }
            override fun onError(error: FacebookException) { Toast.makeText(this@SignupActivity, "Facebook Error: ${error.message}", Toast.LENGTH_SHORT).show() }
        })

        back.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnlogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        btnGoogle.setOnClickListener { googleSignInLauncher.launch(googleSignInClient.signInIntent) }
        btnFB.setOnClickListener { LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile")) }

        btnSignup.setOnClickListener {
            val usrEmail = sEmail.text.toString().trim()
            val usrName = sName.text.toString().trim()
            val usrPass = sPass.text.toString().trim()
            val usrPassC = sPassC.text.toString().trim()

            if (!signupAgree.isChecked) {
                Toast.makeText(this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usrEmail.isEmpty() || usrName.isEmpty() || usrPass.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usrPass != usrPassC) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (usrPass.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(usrEmail, usrPass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                        syncToLocalDatabaseAndGo(usrEmail, usrName)
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) syncToLocalDatabaseAndGo(auth.currentUser?.email ?: "Google User", auth.currentUser?.displayName ?: "Google User")
            else Toast.makeText(this, "Firebase Google Auth Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) syncToLocalDatabaseAndGo(auth.currentUser?.email ?: "Facebook User", auth.currentUser?.displayName ?: "Facebook User")
            else Toast.makeText(this, "Firebase FB Auth Failed", Toast.LENGTH_SHORT).show()
        }
    }

    // NEW: Syncs User to SQLite
    private fun syncToLocalDatabaseAndGo(email: String, name: String) {
        val photoUrl = auth.currentUser?.photoUrl?.toString() ?: ""
        dbHelper.saveOrUpdateUser(email, name, "New Traveler", photoUrl)

        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("User Email", email)
        intent.putExtra("User Name", name)
        intent.putExtra("Is Guest", false)
        startActivity(intent)
        finish()
    }
}