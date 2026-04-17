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
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class ExploreFragment : Fragment() {

    private lateinit var adapter: ExploreAdapter
    private lateinit var fullTripList: List<Trip>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_explore, container, false)

        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.explore_recycler_view)

        // 1. Create the Master Data List (Make sure you have some images in your drawable folder!)
        fullTripList = listOf(
            Trip("Summer in Paris", "Paris, France", "June 2026", "Amazing views of the Eiffel Tower.", R.drawable.ic_image_replacer_foreground, 5.0),
            Trip("Hiking the Alps", "Swiss Alps", "July 2026", "Very cold but beautiful trails.", R.drawable.ic_image_replacer_foreground, 4.5),
            Trip("Beach Vacation", "Maldives", "August 2026", "Relaxing by the crystal clear water.", R.drawable.ic_image_replacer_foreground, 4.8),
            Trip("Tokyo Lights", "Tokyo, Japan", "September 2026", "Exploring the busy city streets.", R.drawable.ic_image_replacer_foreground, 4.9)
        )

        // 2. Setup the RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 3. Attach Adapter and handle clicks (Requirement F2)
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
        recyclerView.adapter = adapter

        // 4. The Search Engine (Requirement F5)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // This runs every single time a letter is typed or deleted!
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filterList(query)
            }
        })

        return view
    }

    // The logic that actually sorts the data for Requirement F5
    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullTripList // If search is empty, show everything
        } else {
            fullTripList.filter { trip ->
                // Check if the typed letters match the title OR the location
                trip.title.contains(query, ignoreCase = true) ||
                        trip.location.contains(query, ignoreCase = true)
            }
        }
        // Tell the adapter to redraw the screen!
        adapter.updateData(filteredList)
    }
}