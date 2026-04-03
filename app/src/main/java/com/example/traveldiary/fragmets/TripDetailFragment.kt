package com.example.traveldiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class TripDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_detail, container, false)

        // 1. UNPACK THE BUNDLE (Requirement F2)
        val trip = arguments?.getSerializable("TRIP_DATA") as? Trip

        // 2. Find ALL the views on your huge detail screen
        val titleView = view.findViewById<TextView>(R.id.detail_title)
        val locationView = view.findViewById<TextView>(R.id.detail_location)
        val dateView = view.findViewById<TextView>(R.id.detail_date)
        val descView = view.findViewById<TextView>(R.id.detail_description)
        val imageView = view.findViewById<ImageView>(R.id.detail_image)

        // 3. Display the data dynamically!
        if (trip != null) {
            titleView.text = trip.title
            locationView.text = trip.location
            dateView.text = trip.date
            descView.text = trip.description

            // Set the image (assuming your Trip model has an imageResId)
            imageView.setImageResource(trip.imageResId)
        }

        // 4. Make the Back Button work
        val backButton = view.findViewById<ImageView>(R.id.detail_back)
        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }
}