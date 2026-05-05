package com.example.traveldiary.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Register a new user in Firebase Auth
    fun registerUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Save user profile details to Cloud Firestore
    fun saveUserToFirestore(email: String, name: String, bio: String, photoUrl: String, onResult: (Boolean, String?) -> Unit) {
        val userId = auth.currentUser?.uid ?: return onResult(false, "User not authenticated")
        
        val userMap = hashMapOf(
            "email" to email,
            "name" to name,
            "bio" to bio,
            "photoUrl" to photoUrl,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId)
            .set(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Login existing user
    fun loginUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    // Check if user is logged in
    fun isUserLoggedIn(): Boolean = auth.currentUser != null
}
