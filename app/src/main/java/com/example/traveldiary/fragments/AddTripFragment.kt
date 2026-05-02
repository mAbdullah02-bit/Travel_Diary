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
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.google.firebase.auth.FirebaseAuth
import java.io.File
import java.io.FileOutputStream

class AddTripFragment : Fragment() {

    private var selectedImageUri: Uri? = null

    // The giant clickable layout
    private lateinit var coverPhotoLayout: FrameLayout
    // The actual image view inside that layout to show the picture
    private lateinit var coverImageView: ImageView

    // 1. LAUNCHER: Open Phone Gallery
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

    // 2. LAUNCHER: Open Camera for Photo
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

    // 3. LAUNCHER: Open Camera for Video
    private val recordVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri: Uri? = result.data?.data
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

        // Find your UI Elements
        val backButton = view.findViewById<ImageView>(R.id.newtrip_back)
        coverPhotoLayout = view.findViewById(R.id.addtrip_cover_layout)
        coverImageView = view.findViewById(R.id.addtrip_cover_image)

        val titleInput = view.findViewById<EditText>(R.id.addtrip_title)
        val locationInput = view.findViewById<EditText>(R.id.addtrip_location)
        val dateInput = view.findViewById<TextView>(R.id.addtrip_date)
        val descInput = view.findViewById<EditText>(R.id.addtrip_desc)

        // NEW: Find the Switch
        val publicSwitch = view.findViewById<SwitchCompat>(R.id.addtrip_public_switch)

        val saveButton = view.findViewById<Button>(R.id.addtrip_save_btn)

        val btnOpenGallery = view.findViewById<LinearLayout>(R.id.btn_open_gallery)
        val btnTakePhoto = view.findViewById<LinearLayout>(R.id.btn_take_photo)
        val btnRecordVideo = view.findViewById<LinearLayout>(R.id.btn_record_video)

        // Catch the Data (Are we Editing or Adding?)
        val isEditMode = arguments?.getBoolean("IS_EDIT_MODE") ?: false
        var editingTripId = -1

        // CALENDAR
        dateInput.setOnClickListener {
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dateInput.text = formattedDate
                },
                year, month, day
            )
            datePickerDialog.show()
        }

        backButton.setOnClickListener {
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
        }

        // --- MEDIA BUTTON CLICKS ---
        coverPhotoLayout.setOnClickListener { pickGalleryImageLauncher.launch("image/*") }
        btnOpenGallery.setOnClickListener { pickGalleryImageLauncher.launch("image/*") }
        btnTakePhoto.setOnClickListener { takePhotoLauncher.launch(null) }
        btnRecordVideo.setOnClickListener {
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            recordVideoLauncher.launch(videoIntent)
        }

        // --- POPULATE IF EDITING ---
        if (isEditMode) {
            val tripToEdit = arguments?.getSerializable("TRIP_DATA") as? Trip

            if (tripToEdit != null) {
                editingTripId = tripToEdit.id

                titleInput.setText(tripToEdit.title)
                locationInput.setText(tripToEdit.location)
                dateInput.setText(tripToEdit.date)
                descInput.setText(tripToEdit.description)
                publicSwitch.isChecked = tripToEdit.isPublic

                if (tripToEdit.imageUri.isNotEmpty()) {
                    try {
                        selectedImageUri = Uri.parse(tripToEdit.imageUri)
                        coverImageView.setImageURI(selectedImageUri)
                        coverImageView.visibility = View.VISIBLE
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                }

                saveButton.text = "Update Trip"
            }
        }

        saveButton.setOnClickListener {
            val tripTitle = titleInput.text.toString().trim()
            val tripLocation = locationInput.text.toString().trim()
            val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

            val tripDate = dateInput.text.toString().trim()
            val tripDescription = descInput.text.toString().trim()
            val coverImageUri = selectedImageUri?.toString() ?: ""
            val isPublic = if (publicSwitch.isChecked) 1 else 0

            if (tripTitle.isEmpty() || tripLocation.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            it.isEnabled = false
            val dbHelper = DatabaseHelper(requireContext())

            // --- FIX 1: DUPLICATE CHECK (Exclude current trip ID if editing) ---
            if (dbHelper.isDuplicateTrip(tripTitle, tripLocation, currentUserEmail, editingTripId)) {
                Toast.makeText(requireContext(), "A trip to this location with this title already exists!", Toast.LENGTH_LONG).show()
                it.isEnabled = true
                return@setOnClickListener
            }

            val success: Boolean
            if (isEditMode) {
                // --- FIX 2: UPDATE MODE ---
                success = dbHelper.updateTrip(
                    editingTripId,
                    tripTitle,
                    tripLocation,
                    tripDate,
                    tripDescription,
                    coverImageUri,
                    isPublic
                )
            } else {
                // --- INSERT MODE ---
                val newRowId = dbHelper.insertTrip(
                    currentUserEmail,
                    tripTitle,
                    tripLocation,
                    tripDate,
                    tripDescription,
                    coverImageUri,
                    isPublic
                )
                success = newRowId != -1L
            }

            if (success) {
                Toast.makeText(requireContext(), if (isEditMode) "Trip Updated!" else "Trip Saved!", Toast.LENGTH_SHORT).show()
                
                // Auto Go-Back
                if (parentFragmentManager.backStackEntryCount > 0) {
                    parentFragmentManager.popBackStack()
                } else {
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
            } else {
                Toast.makeText(requireContext(), "Database Error: Could not save trip.", Toast.LENGTH_SHORT).show()
                it.isEnabled = true
            }
        }

        return view
    }
}
