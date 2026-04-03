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
import com.example.traveldiary.R
import com.example.traveldiary.Adaptors.TripAdapter
import com.example.traveldiary.models.Trip

class HomeFragment : Fragment() {

    // 1. Declare the adapter at the class level so the whole Fragment can see it
    private lateinit var adapter: TripAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler_view)

        // Dummy Data for Home
        val homeTrips = listOf(
            Trip("Home Trip 1", "London, UK", "Oct 2025", "Great trip.", R.drawable.ic_image_replacer_foreground, 4.0),
            Trip("Home Trip 2", "Rome, Italy", "Nov 2025", "Lots of pizza.", R.drawable.ic_image_replacer_foreground, 5.0)
        )

        // 2. Initialize the adapter variable with your data and click listener
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

        if (recyclerView != null) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            // 3. Set the initialized adapter to the RecyclerView
            recyclerView.adapter = adapter
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            // This runs every single time a letter is typed or deleted!
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                filterList(query)
            }

            private fun filterList(query: String) {
                val filteredList = if (query.isEmpty()) {
                    homeTrips // If search is empty, show everything
                } else {
                    homeTrips.filter { trip ->
                        // Check if the typed letters match the title OR the location
                        trip.title.contains(query, ignoreCase = true) ||
                                trip.location.contains(query, ignoreCase = true)
                    }
                }
                // Tell the adapter to redraw the screen!
                adapter.updateData(filteredList)
            }
        })
        return view
    }
}