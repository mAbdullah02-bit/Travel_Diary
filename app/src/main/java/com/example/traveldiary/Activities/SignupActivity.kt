package com.example.traveldiary.Activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.utils.AuthManager
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
import com.google.firebase.auth.UserProfileChangeRequest

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authManager: AuthManager
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private lateinit var dbHelper: DatabaseHelper

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
        authManager = AuthManager()
        dbHelper = DatabaseHelper(this)

        val btnSignup = findViewById<Button>(R.id.signup_BTN)
        val signupAgree = findViewById<CheckBox>(R.id.signup_agree)
        val btnFB = findViewById<Button>(R.id.signup_fbBTN)
        val btnGoogle = findViewById<Button>(R.id.signup_goBTN)
        val btnlogin = findViewById<TextView>(R.id.backtologin)
        val sEmail = findViewById<EditText>(R.id.signup_editmail)
        val sName = findViewById<EditText>(R.id.signup_editname)
        val sPass = findViewById<EditText>(R.id.signup_editpass)
        val sPassC = findViewById<EditText>(R.id.signup_editpassC)
        val back = findViewById<ImageView>(R.id.signup_back)

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

            authManager.registerUser(usrEmail, usrPass) { success, error ->
                if (success) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(usrName)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { _ ->
                        // Save profile to Cloud Firestore (Firebase)
                        authManager.saveUserToFirestore(usrEmail, usrName, "New Traveler", "") { fsSuccess, fsError ->
                            if (fsSuccess) {
                                Toast.makeText(this, "Account Created & Cloud Synced!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "Account Created (Offline Mode)", Toast.LENGTH_SHORT).show()
                                Log.e("SignupActivity", "Firestore sync failed: $fsError")
                            }
                            // Always sync to local DB for offline support and navigate
                            syncToLocalDatabaseAndGo(usrEmail, usrName)
                        }
                    }
                } else {
                    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
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
            if (task.isSuccessful) {
                val user = auth.currentUser
                val email = user?.email ?: ""
                val name = user?.displayName ?: "Traveler"
                val photo = user?.photoUrl?.toString() ?: ""
                
                authManager.saveUserToFirestore(email, name, "Ready to explore!", photo) { _, _ ->
                    syncToLocalDatabaseAndGo(email, name)
                }
            }
            else Toast.makeText(this, "Firebase Google Auth Failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                val email = user?.email ?: ""
                val name = user?.displayName ?: "Traveler"
                val photo = user?.photoUrl?.toString() ?: ""

                authManager.saveUserToFirestore(email, name, "Ready to explore!", photo) { _, _ ->
                    syncToLocalDatabaseAndGo(email, name)
                }
            }
            else Toast.makeText(this, "Firebase FB Auth Failed", Toast.LENGTH_SHORT).show()
        }
    }

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
