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
import com.example.traveldiary.Adaptors.ExploreAdapter
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class ExploreFragment : Fragment() {

    private lateinit var adapter: ExploreAdapter
    private var fullTripList = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_explore, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.explore_recycler_view)

        adapter = ExploreAdapter(fullTripList) { clickedTrip ->
            val detailFragment = ExploreDetailFragment()
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
                filterList(s.toString())
            }
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPublicTrips()
    }

    private fun loadPublicTrips() {
        val dbHelper = DatabaseHelper(requireContext())
        val cursor = dbHelper.getPublicTrips() // NEW: Fetches community trips!

        fullTripList.clear()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val trip = Trip(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("cover_image")),
                    isPublic = true // Always true because of the SQL query
                )
                fullTripList.add(trip)
            } while (cursor.moveToNext())
            cursor.close()
        }
        adapter.updateData(fullTripList)
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullTripList
        } else {
            fullTripList.filter { trip ->
                trip.title.contains(query, ignoreCase = true) || trip.location.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(filteredList)
    }
}