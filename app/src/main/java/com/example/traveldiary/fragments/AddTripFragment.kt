package com.example.traveldiary.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.example.traveldiary.utils.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddTripFragment : Fragment() {

    private var selectedImageUri: Uri? = null
    private val firestoreHelper = FirestoreHelper()

    private lateinit var coverPhotoLayout: FrameLayout
    private lateinit var coverImageView: ImageView

    private val pickGalleryImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val internalUri = saveImageToInternalStorage(it)
            if (internalUri != null) {
                selectedImageUri = internalUri
                coverImageView.setImageURI(internalUri)
                coverImageView.visibility = View.VISIBLE
            }
        }
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            val internalUri = saveBitmapToInternalStorage(it)
            if (internalUri != null) {
                selectedImageUri = internalUri
                coverImageView.setImageBitmap(it)
                coverImageView.visibility = View.VISIBLE
            }
        }
    }

    private val recordVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Toast.makeText(requireContext(), "Video captured successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().filesDir, "trip_${System.currentTimeMillis()}.jpg")
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

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri? {
        return try {
            val file = File(requireContext().filesDir, "trip_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
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
        val view = inflater.inflate(R.layout.activity_addtrip, container, false)

        val backButton = view.findViewById<ImageView>(R.id.newtrip_back)
        coverPhotoLayout = view.findViewById(R.id.addtrip_cover_layout)
        coverImageView = view.findViewById(R.id.addtrip_cover_image)

        val titleInput = view.findViewById<EditText>(R.id.addtrip_title)
        val locationInput = view.findViewById<EditText>(R.id.addtrip_location)
        val dateInput = view.findViewById<TextView>(R.id.addtrip_date)
        val descInput = view.findViewById<EditText>(R.id.addtrip_desc)
        val publicSwitch = view.findViewById<SwitchCompat>(R.id.addtrip_public_switch)
        val saveButton = view.findViewById<Button>(R.id.addtrip_save_btn)

        val btnOpenGallery = view.findViewById<LinearLayout>(R.id.btn_open_gallery)
        val btnTakePhoto = view.findViewById<LinearLayout>(R.id.btn_take_photo)
        val btnRecordVideo = view.findViewById<LinearLayout>(R.id.btn_record_video)

        val isEditMode = arguments?.getBoolean("IS_EDIT_MODE") ?: false
        val currentTrip = arguments?.getSerializable("TRIP_DATA") as? Trip

        dateInput.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, y, m, d -> dateInput.text = "$d/${m + 1}/$y" },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        // FIX 1: Modern foolproof back navigation
        backButton.setOnClickListener {
            // Check if there is a history to go back to
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // If history is broken, force it back to Home
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }
        coverPhotoLayout.setOnClickListener { pickGalleryImageLauncher.launch("image/*") }
        btnOpenGallery.setOnClickListener { pickGalleryImageLauncher.launch("image/*") }
        btnTakePhoto.setOnClickListener { takePhotoLauncher.launch(null) }
        btnRecordVideo.setOnClickListener {
            recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE))
        }

        if (isEditMode && currentTrip != null) {
            titleInput.setText(currentTrip.title)
            locationInput.setText(currentTrip.location)
            dateInput.setText(currentTrip.date)
            descInput.setText(currentTrip.description)
            publicSwitch.isChecked = currentTrip.isPublic

            if (currentTrip.imageUri.isNotEmpty()) {
                selectedImageUri = Uri.parse(currentTrip.imageUri)
                coverImageView.setImageURI(selectedImageUri)
                coverImageView.visibility = View.VISIBLE
            }
            saveButton.text = "Update Trip"
        }

        saveButton.setOnClickListener {
            val title = titleInput.text.toString().trim()
            val location = locationInput.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser
            val email = user?.email ?: "guest@example.com"
            val name = user?.displayName ?: "Traveler"

            if (title.isEmpty() || location.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            it.isEnabled = false
            val dbHelper = DatabaseHelper(requireContext())

            // FIX 2: Run saving on background thread, navigate on Main thread
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val tripToSave = if (isEditMode && currentTrip != null) {
                        currentTrip.copy(
                            title = title,
                            location = location,
                            date = dateInput.text.toString(),
                            description = descInput.text.toString(),
                            imageUri = selectedImageUri?.toString() ?: currentTrip.imageUri, // Retain old image if no new one selected
                            isPublic = publicSwitch.isChecked
                        )
                    } else {
                        Trip(
                            userEmail = email,
                            authorName = name,
                            title = title,
                            location = location,
                            date = dateInput.text.toString(),
                            description = descInput.text.toString(),
                            imageUri = selectedImageUri?.toString() ?: "",
                            isPublic = publicSwitch.isChecked
                        )
                    }

                    // 1. Sync to Firestore
                    val firestoreId = firestoreHelper.saveTrip(tripToSave)

                    // 2. Sync to Local SQLite
                    if (isEditMode && currentTrip != null) {
                        dbHelper.updateTrip(
                            currentTrip.id,
                            firestoreId,
                            title,
                            location,
                            dateInput.text.toString(),
                            descInput.text.toString(),
                            selectedImageUri?.toString() ?: currentTrip.imageUri, // Retain old image
                            if (publicSwitch.isChecked) 1 else 0
                        )
                    } else {
                        dbHelper.insertTrip(
                            firestoreId,
                            email,
                            title,
                            location,
                            dateInput.text.toString(),
                            descInput.text.toString(),
                            selectedImageUri?.toString() ?: "",
                            if (publicSwitch.isChecked) 1 else 0
                        )
                    }

                    // Complete the action back on the UI thread
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Saved successfully!", Toast.LENGTH_SHORT).show()
                        if (parentFragmentManager.backStackEntryCount > 0) {
                            parentFragmentManager.popBackStack()
                        } else {
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, HomeFragment())
                                .commit()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        it.isEnabled = true
                    }
                }
            }
        }
        return view
    }
}