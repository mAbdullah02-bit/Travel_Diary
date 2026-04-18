package com.example.traveldiary.fragments

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
import com.example.traveldiary.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var profileInitialText: TextView
    private lateinit var profileNameText: TextView

    // LAUNCHER: Pick Image from Gallery
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            profileImageView.setImageURI(uri)
            profileImageView.visibility = View.VISIBLE
            profileInitialText.visibility = View.GONE
            Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_profile, container, false)

        // Find UI Elements
        val profilePicContainer = view.findViewById<FrameLayout>(R.id.profile_pic_container)
        profileImageView = view.findViewById(R.id.profile_image_view)
        profileInitialText = view.findViewById(R.id.profile_initial_text)
        profileNameText = view.findViewById(R.id.profile_name_text)

        // Action Rows
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btn_edit_profile)
        val btnChangePassword = view.findViewById<LinearLayout>(R.id.btn_change_password)
        val btnLocationSettings = view.findViewById<LinearLayout>(R.id.btn_location_settings)
        val btnStorageSettings = view.findViewById<LinearLayout>(R.id.btn_storage_settings)
        val btnFAQ = view.findViewById<LinearLayout>(R.id.profile_support)
        val btnAbout = view.findViewById<LinearLayout>(R.id.btn_about)

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

        return view
    }

    // --- DIALOG & INTENT FUNCTIONS ---

    private fun showEditProfileDialog() {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val nameInput = EditText(context).apply {
            hint = "Full Name"
            setText(profileNameText.text.toString()) // Pre-fills with current name
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
                if (newName.isNotEmpty()) {
                    profileNameText.text = newName
                    profileInitialText.text = newName.first().toString().uppercase()
                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
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

        val oldPassInput = EditText(context).apply { hint = "Current Password" }
        val newPassInput = EditText(context).apply { hint = "New Password" }

        layout.addView(oldPassInput)
        layout.addView(newPassInput)

        MaterialAlertDialogBuilder(context)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                // TODO: Wire this to your Firebase Authentication!
                Toast.makeText(context, "Password updated successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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