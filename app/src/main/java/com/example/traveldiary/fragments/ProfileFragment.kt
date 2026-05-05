package com.example.traveldiary.fragments

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.traveldiary.Activities.LoginActivity
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.utils.FirestoreHelper
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var profileInitialText: TextView
    private lateinit var profileNameText: TextView
    private lateinit var profileEmailText: TextView
    
    // Dashboard TextViews
    private lateinit var tripsCountText: TextView
    private lateinit var photosCountText: TextView
    private lateinit var placesCountText: TextView

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var auth: FirebaseAuth
    private val firestoreHelper = FirestoreHelper()

    // LAUNCHER: Pick Image from Gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = saveImageToInternalStorage(it)
            if (internalUri != null) {
                profileImageView.setImageURI(internalUri)
                profileImageView.visibility = View.VISIBLE
                profileInitialText.visibility = View.GONE

                // Save image choice to SQLite Database
                val currentUserEmail = auth.currentUser?.email ?: "guest@example.com"
                dbHelper.saveOrUpdateUser(currentUserEmail, profileNameText.text.toString(), "", internalUri.toString())
                Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
                updateDashboard() // Refresh dashboard if photo count changed
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, "profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        // Initialize Backend
        dbHelper = DatabaseHelper(requireContext())
        auth = FirebaseAuth.getInstance()

        // Find UI Elements
        val profilePicContainer = view.findViewById<FrameLayout>(R.id.profile_pic_container)
        profileImageView = view.findViewById(R.id.profile_image_view)
        profileInitialText = view.findViewById(R.id.profile_initial_text)
        profileNameText = view.findViewById(R.id.profile_name_text)
        profileEmailText = view.findViewById<TextView>(R.id.profile_email_text)
        
        // Dashboard
        tripsCountText = view.findViewById(R.id.profile_trips_count)
        photosCountText = view.findViewById(R.id.profile_photos_count)
        placesCountText = view.findViewById(R.id.profile_places_count)

        // Action Rows
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btn_edit_profile)
        val btnChangePassword = view.findViewById<LinearLayout>(R.id.btn_change_password)
        val btnLocationSettings = view.findViewById<LinearLayout>(R.id.btn_location_settings)
        val btnStorageSettings = view.findViewById<LinearLayout>(R.id.btn_storage_settings)
        val btnFAQ = view.findViewById<LinearLayout>(R.id.profile_support)
        val btnAbout = view.findViewById<LinearLayout>(R.id.btn_about)
        val btnLogout = view.findViewById<MaterialCardView>(R.id.btn_logout)

        // Display current Firebase email
        val currentUserEmail = auth.currentUser?.email ?: "guest@example.com"
        profileEmailText.text = currentUserEmail

        // Load saved Name & Photo from SQLite Database
        loadUserProfileData(currentUserEmail)
        updateDashboard()

        // --- CLICK LISTENERS ---

        profilePicContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        btnLocationSettings.setOnClickListener {
            openAppPermissionsSettings()
        }

        btnStorageSettings.setOnClickListener {
            openAppPermissionsSettings()
        }

        btnFAQ.setOnClickListener {
            showFAQDialog()
        }

        btnAbout.setOnClickListener {
            showAboutDialog()
        }

        btnLogout.setOnClickListener {
            performLogout()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        updateDashboard()
    }

    private fun updateDashboard() {
        val currentUserEmail = auth.currentUser?.email ?: "guest@example.com"
        
        val tripCount = dbHelper.getTripCountForUser(currentUserEmail)
        val photoCount = dbHelper.getPhotoCountForUser(currentUserEmail)
        val placeCount = dbHelper.getPlaceCountForUser(currentUserEmail)
        
        tripsCountText.text = tripCount.toString()
        photosCountText.text = photoCount.toString()
        placesCountText.text = placeCount.toString()
    }

    private fun loadUserProfileData(email: String) {
        val cursor = dbHelper.getUserProfile(email)
        if (cursor != null && cursor.moveToFirst()) {
            val savedName = cursor.getString(cursor.getColumnIndexOrThrow("name"))
            val savedPicUri = cursor.getString(cursor.getColumnIndexOrThrow("profile_pic"))

            if (savedName.isNotEmpty()) {
                profileNameText.text = savedName
                profileInitialText.text = savedName.first().toString().uppercase()
            }

            if (savedPicUri.isNotEmpty()) {
                try {
                    profileImageView.setImageURI(Uri.parse(savedPicUri))
                    profileImageView.visibility = View.VISIBLE
                    profileInitialText.visibility = View.GONE
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            cursor.close()
        }
    }

    private fun showEditProfileDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(context).apply {
            hint = "Full Name"
            setText(profileNameText.text.toString())
        }

        val bioInput = EditText(context).apply {
            hint = "Bio (e.g. Travel Enthusiast)"
        }

        layout.addView(nameInput)
        layout.addView(bioInput)

        MaterialAlertDialogBuilder(context)
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newBio = bioInput.text.toString().trim()
                if (newName.isNotEmpty()) {
                    profileNameText.text = newName
                    profileInitialText.text = newName.first().toString().uppercase()

                    // Requirement F2: Update User document in Firestore
                    val currentUserEmail = auth.currentUser?.email ?: return@setPositiveButton
                    lifecycleScope.launch {
                        try {
                            firestoreHelper.updateUserProfile(currentUserEmail, newName, newBio)
                            dbHelper.saveOrUpdateUser(currentUserEmail, newName, newBio, "")
                            Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val newPassInput = EditText(context).apply { hint = "Enter New Password" }
        layout.addView(newPassInput)

        MaterialAlertDialogBuilder(context)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val newPassword = newPassInput.text.toString().trim()

                if (newPassword.length >= 8) {
                    val user = auth.currentUser
                    user?.updatePassword(newPassword)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Requirement F2: Update lastPasswordChange in Firestore
                                lifecycleScope.launch {
                                    try {
                                        firestoreHelper.updatePasswordChangeTimestamp(user.email!!)
                                        Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        println("Error updating timestamp: ${e.message}")
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                } else {
                    Toast.makeText(context, "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        val sharedPrefs = requireContext().getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showFAQDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Help & FAQ")
            .setMessage("Q: How do I create a new trip?\nA: Go to the home screen and press the '+' button.\n\nQ: Are my trips public?\nA: No, all journals are private by default unless you toggle the public setting.")
            .setPositiveButton("Close", null)
            .show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About Travel Diary")
            .setMessage("Version: 1.0.0\n\nBuilt to help you log your memories, track your destinations, and secure your travel experiences safely.")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun openAppPermissionsSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }
}
