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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import android.widget.FrameLayout
import android.widget.TextView

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
            coverImageView.setImageURI(uri) // Pass the image to the ImageView inside the layout
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

        // NEW: The Cover layout (Clickable) and the Image inside it (Display)
        // Ensure you have BOTH of these IDs in your XML!
        coverPhotoLayout = view.findViewById(R.id.addtrip_cover_layout)
        coverImageView = view.findViewById(R.id.addtrip_cover_image)

        val titleInput = view.findViewById<EditText>(R.id.addtrip_title)
        val locationInput = view.findViewById<EditText>(R.id.addtrip_location)
        val dateInput = view.findViewById<TextView>(R.id.addtrip_date)
        val descInput = view.findViewById<EditText>(R.id.addtrip_desc)
        val saveButton = view.findViewById<Button>(R.id.addtrip_save_btn)

        // NEW: Bottom three action buttons updated to LinearLayouts!
        val btnOpenGallery = view.findViewById<LinearLayout>(R.id.btn_open_gallery)
        val btnTakePhoto = view.findViewById<LinearLayout>(R.id.btn_take_photo)
        val btnRecordVideo = view.findViewById<LinearLayout>(R.id.btn_record_video)

        // Catch the Data (Are we Editing or Adding?)
        val isEditMode = arguments?.getBoolean("IS_EDIT_MODE") ?: false
        var editingTripId = -1


        // CALANDAR
        dateInput.setOnClickListener {
            // Get today's date to set as the default on the calendar
            val calendar = java.util.Calendar.getInstance()
            val year = calendar.get(java.util.Calendar.YEAR)
            val month = calendar.get(java.util.Calendar.MONTH)
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

            // Open the official Android Date Picker
            val datePickerDialog = android.app.DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format the selected date and set it to the TextView
                    // Note: Months are 0-indexed, so we add 1
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    dateInput.text = formattedDate
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // --- MEDIA BUTTON CLICKS ---

        // Clicking anywhere on the Cover Layout opens the gallery
        coverPhotoLayout.setOnClickListener {
            pickGalleryImageLauncher.launch("image/*")
        }

        btnOpenGallery.setOnClickListener {
            pickGalleryImageLauncher.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {
            takePhotoLauncher.launch(null)
        }

        btnRecordVideo.setOnClickListener {
            val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            recordVideoLauncher.launch(videoIntent)
        }

        // --- POPULATE AND SAVE LOGIC ---

        if (isEditMode) {
            val tripToEdit = arguments?.getSerializable("TRIP_DATA") as? Trip

            if (tripToEdit != null) {
                titleInput.setText(tripToEdit.title)
                locationInput.setText(tripToEdit.location)
                dateInput.setText(tripToEdit.date)
                descInput.setText(tripToEdit.description)

                saveButton.text = "Update Trip"
            }
        }

        saveButton.setOnClickListener {
            val newTitle = titleInput.text.toString().trim()
            val newLocation = locationInput.text.toString().trim()
            val newDate = dateInput.text.toString().trim()
            val newDesc = descInput.text.toString().trim()

            if (newTitle.isEmpty() || newLocation.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the title and location!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                // TODO: Call your JAVA SQLite Database Helper to UPDATE the existing trip here
                Toast.makeText(requireContext(), "Trip Updated!", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: Call your JAVA SQLite Database Helper to INSERT a new trip here
                Toast.makeText(requireContext(), "New Trip Saved!", Toast.LENGTH_SHORT).show()
            }

            parentFragmentManager.popBackStack()
        }

        return view
    }
}