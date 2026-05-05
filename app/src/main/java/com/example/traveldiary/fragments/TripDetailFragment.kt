package com.example.traveldiary.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.example.traveldiary.utils.FirestoreHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripDetailFragment : Fragment() {

    private lateinit var titleView: TextView
    private lateinit var locationView: TextView
    private lateinit var dateView: TextView
    private lateinit var descView: TextView
    private lateinit var imageView: ImageView

    private var currentTripId: Int = -1
    private var currentTrip: Trip? = null
    private val firestoreHelper = FirestoreHelper()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.trip_detail, container, false)

        currentTrip = arguments?.getSerializable("TRIP_DATA") as? Trip
        if (currentTrip != null) {
            currentTripId = currentTrip!!.id
        }

        titleView = view.findViewById(R.id.detail_title)
        locationView = view.findViewById(R.id.detail_location)
        dateView = view.findViewById(R.id.detail_date)
        descView = view.findViewById(R.id.detail_description)
        imageView = view.findViewById(R.id.detail_image)

        val editButton = view.findViewById<Button>(R.id.detail_edit_btn)
        val deleteButton = view.findViewById<Button>(R.id.detail_delete_btn)
        val backButton = view.findViewById<ImageView>(R.id.detail_back)

        if (currentTrip != null) {
            updateUI(currentTrip!!)
        }

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        editButton.setOnClickListener {
            if (currentTrip != null) {
                val addTripFragment = AddTripFragment()
                val bundle = Bundle()
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
            currentTrip?.let { trip ->
                Toast.makeText(requireContext(), "Deleting trip...", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()

                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        if (trip.tripId.isNotEmpty()) {
                            firestoreHelper.deleteTrip(trip.tripId)
                        }
                        val dbHelper = DatabaseHelper(requireContext())
                        dbHelper.deleteTrip(trip.id)
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            println("Background delete failed: ${e.message}")
                        }
                    }
                }
            }
        }

        return view
    }

    private fun updateUI(trip: Trip) {
        titleView.text = trip.title
        locationView.text = trip.location
        dateView.text = trip.date
        descView.text = trip.description

        // Fix: Glide integration for grayscale placeholder
        val placeholder = ColorDrawable(Color.parseColor("#D3D3D3"))

        val imageSource = when {
            trip.imageUrl.isNotEmpty() -> trip.imageUrl
            trip.imageUri.isNotEmpty() -> trip.imageUri
            else -> null
        }

        Glide.with(requireContext())
            .load(imageSource)
            .placeholder(placeholder)
            .error(placeholder)
            .into(imageView)
    }

    // Fix: Fetch fresh data from Firestore directly to prevent stale data
    override fun onResume() {
        super.onResume()
        val tripId = currentTrip?.tripId

        if (!tripId.isNullOrEmpty()) {
            FirebaseFirestore.getInstance().collection("trips")
                .document(tripId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val freshTrip = document.toObject(Trip::class.java)
                        if (freshTrip != null) {
                            currentTrip = freshTrip
                            updateUI(freshTrip)
                        }
                    }
                }
                .addOnFailureListener {
                    println("Failed to fetch updated trip: ${it.message}")
                }
        }
    }
}