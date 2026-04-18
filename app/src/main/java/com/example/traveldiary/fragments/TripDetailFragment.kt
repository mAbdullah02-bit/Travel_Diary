package com.example.traveldiary.fragments

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
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.google.android.material.snackbar.Snackbar

class TripDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_detail, container, false)

        // 1. UNPACK THE BUNDLE
        val trip = arguments?.getSerializable("TRIP_DATA") as? Trip

        // 2. Find ALL the views
        val titleView = view.findViewById<TextView>(R.id.detail_title)
        val locationView = view.findViewById<TextView>(R.id.detail_location)
        val dateView = view.findViewById<TextView>(R.id.detail_date)
        val descView = view.findViewById<TextView>(R.id.detail_description)
        val imageView = view.findViewById<ImageView>(R.id.detail_image)

        // Find our new Action Buttons
        val editButton = view.findViewById<Button>(R.id.detail_edit_btn)
        val deleteButton = view.findViewById<Button>(R.id.detail_delete_btn)
        val backButton = view.findViewById<ImageView>(R.id.detail_back)

        // 3. Display the data dynamically
        if (trip != null) {
            titleView.text = trip.title
            locationView.text = trip.location
            dateView.text = trip.date
            descView.text = trip.description
            imageView.setImageResource(trip.imageResId)
        }

        // 4. Make the Back Button work
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 5. EDIT BUTTON LOGIC
        editButton.setOnClickListener {
            if (trip != null) {
                val addTripFragment = AddTripFragment()

                // Pack the trip data back up to send to the Add Screen
                val bundle = Bundle()
                bundle.putSerializable("TRIP_DATA", trip)
                bundle.putBoolean("IS_EDIT_MODE", true) // Flag to tell AddTripFragment we are editing
                addTripFragment.arguments = bundle

                // Navigate to AddTripFragment
                // NOTE: Change R.id.fragment_container to whatever your main activity container ID is!
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, addTripFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        // 6. DELETE BUTTON LOGIC
        deleteButton.setOnClickListener {
            if (trip != null) {
                // TODO: Add your SQLite/Firebase delete function here!
                // dbHelper.deleteTrip(trip.id)

                // Show the "Trip Deleted" card
                Snackbar.make(view, "Trip Deleted Successfully", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(resources.getColor(android.R.color.holo_red_dark, null))
                    .setTextColor(resources.getColor(android.R.color.white, null))
                    .show()

                // Wait exactly 1 second (1000 milliseconds) then go back home
                Handler(Looper.getMainLooper()).postDelayed({
                    parentFragmentManager.popBackStack()
                }, 1000)
            }
        }

        return view
    }
}