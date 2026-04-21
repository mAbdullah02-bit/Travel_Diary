package com.example.traveldiary.fragments

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.google.android.material.snackbar.Snackbar

class TripDetailFragment : Fragment() {

    // We make these global so we can update them when the screen refreshes
    private lateinit var titleView: TextView
    private lateinit var locationView: TextView
    private lateinit var dateView: TextView
    private lateinit var descView: TextView
    private lateinit var imageView: ImageView

    private var currentTripId: Int = -1
    private var currentTrip: Trip? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_detail, container, false)

        // 1. Unpack the initial data sent from the Home Screen
        currentTrip = arguments?.getSerializable("TRIP_DATA") as? Trip
        if (currentTrip != null) {
            currentTripId = currentTrip!!.id
        }

        // 2. Find ALL the views
        titleView = view.findViewById(R.id.detail_title)
        locationView = view.findViewById(R.id.detail_location)
        dateView = view.findViewById(R.id.detail_date)
        descView = view.findViewById(R.id.detail_description)
        imageView = view.findViewById(R.id.detail_image)

        val editButton = view.findViewById<Button>(R.id.detail_edit_btn)
        val deleteButton = view.findViewById<Button>(R.id.detail_delete_btn)
        val backButton = view.findViewById<ImageView>(R.id.detail_back)

        // 3. Populate initial data
        if (currentTrip != null) {
            titleView.text = currentTrip!!.title
            locationView.text = currentTrip!!.location
            dateView.text = currentTrip!!.date
            descView.text = currentTrip!!.description

            if (currentTrip!!.imageUri.isNotEmpty()) {
                imageView.setImageURI(Uri.parse(currentTrip!!.imageUri))
            } else {
                imageView.setImageResource(currentTrip!!.imageResId)
            }
        }

        // 4. Button Clicks
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        editButton.setOnClickListener {
            if (currentTrip != null) {
                val addTripFragment = AddTripFragment()
                val bundle = Bundle()
                // Send the currentTrip (which will be updated by onResume if edited previously)
                bundle.putSerializable("TRIP_DATA", currentTrip)
                bundle.putBoolean("IS_EDIT_MODE", true)
                addTripFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, addTripFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        deleteButton.setOnClickListener {
            if (currentTripId != -1) {
                val dbHelper = DatabaseHelper(requireContext())
                val success = dbHelper.deleteTrip(currentTripId)

                if (success) {
                    Snackbar.make(view, "Trip Deleted Successfully", Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
                        .setTextColor(resources.getColor(android.R.color.white, null))
                        .show()

                    Handler(Looper.getMainLooper()).postDelayed({
                        parentFragmentManager.popBackStack()
                    }, 1000)
                } else {
                    Snackbar.make(view, "Error deleting trip", Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        return view
    }

    // 5. THE FIX: Fetch fresh data from the database every time this screen becomes visible!
    override fun onResume() {
        super.onResume()

        if (currentTripId != -1) {
            val dbHelper = DatabaseHelper(requireContext())
            val cursor = dbHelper.getSingleTrip(currentTripId)

            if (cursor != null && cursor.moveToFirst()) {
                val freshTitle = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val freshLocation = cursor.getString(cursor.getColumnIndexOrThrow("location"))
                val freshDate = cursor.getString(cursor.getColumnIndexOrThrow("date"))
                val freshDesc = cursor.getString(cursor.getColumnIndexOrThrow("description"))
                val freshImageUri = cursor.getString(cursor.getColumnIndexOrThrow("cover_image"))
                val isPublic = cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1

                // Update the screen with the fresh data
                titleView.text = freshTitle
                locationView.text = freshLocation
                dateView.text = freshDate
                descView.text = freshDesc

                if (freshImageUri.isNotEmpty()) {
                    imageView.setImageURI(Uri.parse(freshImageUri))
                }

                // Update our in-memory object so if they click "Edit" again, it sends the new data!
                currentTrip?.title = freshTitle
                currentTrip?.location = freshLocation
                currentTrip?.date = freshDate
                currentTrip?.description = freshDesc
                currentTrip?.imageUri = freshImageUri
                currentTrip?.isPublic = isPublic

                cursor.close()
            }
        }
    }
}