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
class AddTripFragment : Fragment() {

    private var selectedImageUri: Uri? = null

    // The giant clickable layout
    private lateinit var coverPhotoLayout: FrameLayout
    // The actual image view inside that layout to show the picture
    private lateinit var coverImageView: ImageView

    // 1. LAUNCHER: Open Phone Gallery
    private val pickGalleryImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            coverImageView.setImageURI(uri)
            coverImageView.visibility = View.VISIBLE
        }
    }

    // 2. LAUNCHER: Open Camera for Photo
    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        if (bitmap != null) {
            coverImageView.setImageBitmap(bitmap)
            coverImageView.visibility = View.VISIBLE
        }
    }

    // 3. LAUNCHER: Open Camera for Video
    private val recordVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val videoUri: Uri? = result.data?.data
            Toast.makeText(requireContext(), "Video captured successfully!", Toast.LENGTH_SHORT).show()
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
            parentFragmentManager.popBackStack()
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
                publicSwitch.isChecked = tripToEdit.isPublic // Set the switch state!

                if (tripToEdit.imageUri.isNotEmpty()) {
                    selectedImageUri = Uri.parse(tripToEdit.imageUri)
                    coverImageView.setImageURI(selectedImageUri)
                    coverImageView.visibility = View.VISIBLE
                }

                saveButton.text = "Update Trip"
            }
        }

        // --- SAVE TO DATABASE ---
        saveButton.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            val newLocation = locationInput.text.toString().trim()
            val newDate = dateInput.text.toString().trim()
            val newDesc = descInput.text.toString().trim()

            // Convert boolean to integer for SQLite (1 for True/Public, 0 for False/Private)
            val isPublicInt = if (publicSwitch.isChecked) 1 else 0

            if (newTitle.isEmpty() || newLocation.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the title and location!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val dbHelper = DatabaseHelper(requireContext())
            val imagePathString = selectedImageUri?.toString() ?: ""

            // TODO: Replace this hardcoded string with FirebaseAuth.getInstance().currentUser?.email
            val currentUserEmail = "test@example.com"

            if (isEditMode) {
                // Call upgraded updateTrip method
                val success = dbHelper.updateTrip(editingTripId, newTitle, newLocation, newDate, newDesc, imagePathString, isPublicInt)
                if (success) {
                    Toast.makeText(requireContext(), "Trip Updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error updating trip", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Call upgraded insertTrip method with userEmail and isPublicInt
                val resultId = dbHelper.insertTrip(currentUserEmail, newTitle, newLocation, newDate, newDesc, imagePathString, isPublicInt)
                if (resultId != -1L) {
                    Toast.makeText(requireContext(), "New Trip Saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Error saving trip", Toast.LENGTH_SHORT).show()
                }
            }

            parentFragmentManager.popBackStack()
        }

        return view
    }
}