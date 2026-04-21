package com.example.traveldiary.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.Adaptors.TripAdapter
import com.example.traveldiary.models.Trip
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var adapter: TripAdapter
    private var homeTrips = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler_view)

        adapter = TripAdapter(homeTrips) { clickedTrip ->
            val detailFragment = TripDetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("TRIP_DATA", clickedTrip)
            detailFragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filterList(query)
            }
        })
        val fabAi = view.findViewById<View>(R.id.fab_ai_chat)
        fabAi.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatbotFragment())
                .addToBackStack(null)
                .commit()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        injectDummyData()  // seeding
        loadTripsFromDatabase()
    }
    private fun loadTripsFromDatabase() {
        val dbHelper = DatabaseHelper(requireContext())
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        val cursor = dbHelper.getUserTrips(currentUserEmail)

        // Create a local temporary list to hold fresh data
        val freshTrips = mutableListOf<Trip>()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val trip = Trip(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("cover_image")) ?: "",
                    isPublic = cursor.getInt(cursor.getColumnIndexOrThrow("is_public")) == 1
                )
                freshTrips.add(trip)
            } while (cursor.moveToNext())
            cursor.close()
        }

        // --- CRITICAL FIX ---
        // Update the master list AND tell the adapter to refresh
        homeTrips.clear()
        homeTrips.addAll(freshTrips)
        adapter.updateData(homeTrips)
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            homeTrips
        } else {
            homeTrips.filter { trip ->
                trip.title.contains(query, ignoreCase = true) || trip.location.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filteredList)
    }
    // --- TEMPORARY DATABASE SEEDING ---
    private fun injectDummyData() {
        val dbHelper = DatabaseHelper(requireContext())
        val currentUserEmail = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        // Check if the database is empty before injecting so we don't accidentally create hundreds of duplicates!
        val cursor = dbHelper.getUserTrips(currentUserEmail)
        val isEmpty = (cursor == null || cursor.count == 0)
        cursor?.close()

        if (isEmpty) {

            dbHelper.insertTrip(currentUserEmail, "Summer in Paris", "Paris, France", "15/06/2026", "Ate way too many croissants near the Eiffel Tower. The weather was perfect.", "", 1)
            dbHelper.insertTrip(currentUserEmail, "Hiking the Alps", "Swiss Alps", "12/07/2026", "Beautiful trails, freezing peaks, and amazing hot chocolate.", "", 1)
            dbHelper.insertTrip(currentUserEmail, "Beach Retreat", "Maldives", "05/08/2026", "Crystal clear water. Spent three days just reading on the sand.", "", 1)
            dbHelper.insertTrip(currentUserEmail, "Tokyo Neon Lights", "Tokyo, Japan", "10/09/2026", "Explored Akihabara and ate the best sushi of my life.", "", 1)
            dbHelper.insertTrip(currentUserEmail, "New York Minute", "New York, USA", "20/12/2026", "Times Square was incredibly crowded, but seeing the holiday lights was worth it.", "", 1)
        }
    }
}