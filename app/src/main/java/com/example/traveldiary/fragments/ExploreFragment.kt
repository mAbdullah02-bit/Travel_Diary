package com.example.traveldiary.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.Adaptors.ExploreAdapter
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.URL
import java.util.Locale

class ExploreFragment : Fragment() {

    private lateinit var adapter: ExploreAdapter
    private var fullTripList = mutableListOf<Trip>()
    
    // Dashboard TextViews
    private lateinit var totalTripsText: TextView
    private lateinit var totalPhotosText: TextView
    private lateinit var totalPlacesText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_explore, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.explore_recycler_view)
        
        // Find Dashboard IDs
        totalTripsText = view.findViewById(R.id.explore_trips_count)
        totalPhotosText = view.findViewById(R.id.explore_photos_count)
        totalPlacesText = view.findViewById(R.id.explore_places_count)

        // Fixed: Removed incorrect mutableListOf() argument and specified Trip type
        adapter = ExploreAdapter { clickedTrip: Trip ->
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fetch fresh inspiration from a REST API
//        fetchDailyInspiration()
    }

//    private fun fetchDailyInspiration() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val quoteText = withContext(Dispatchers.IO) {
//                    Log.d("ExploreFragment", "Fetching inspiration from API...")
//                    // ZenQuotes API returns a JSON array: [{"q":"quote", "a":"author", ...}]
//                    val response = URL("https://zenquotes.io/api/quotes").readText()
//
//                    // Properly parse the JSON array response
//                    val jsonArray = JSONArray(response)
//                    if (jsonArray.length() > 0) {
//                        val firstItem = jsonArray.getJSONObject(0)
//                        val quote = firstItem.getString("q")
//                        val author = firstItem.getString("a")
//                        "\"$quote\" — $author"
//                    } else {
//                        "Discover travel stories from around the world"
//                    }
//                }
//                Log.d("ExploreFragment", "Quote received: $quoteText")
//                // Update the subtitle with the properly parsed quote
//                view?.findViewById<TextView>(R.id.explore_subtitle)?.text = quoteText
//                context?.let {
//                    Toast.makeText(it, "New Inspiration Loaded!", Toast.LENGTH_SHORT).show()
//                }
//            } catch (e: Exception) {
//                Log.e("ExploreFragment", "Error fetching inspiration", e)
//            }
//        }
//    }

    override fun onResume() {
        super.onResume()
        // TODO: In the future, this will fetch from Firebase Firestore instead of SQLite
        loadPublicTrips()
        updateExploreDashboard()
    }

    private fun updateExploreDashboard() {
        val dbHelper = DatabaseHelper(requireContext())
        
        // Fetching real overall public data from SQLite
        val totalTrips = dbHelper.getTotalPublicTripCount()
        val totalPhotos = dbHelper.getTotalPublicPhotoCount()
        val totalPlaces = dbHelper.getTotalPublicPlaceCount()
        
        totalTripsText.text = formatCount(totalTrips)
        totalPhotosText.text = formatCount(totalPhotos)
        totalPlacesText.text = formatCount(totalPlaces)
    }
    
    private fun formatCount(count: Int): String {
        return if (count >= 1000) {
            String.format(Locale.US, "%.1fk", count / 1000.0)
        } else {
            count.toString()
        }
    }

    private fun loadPublicTrips() {
        val dbHelper = DatabaseHelper(requireContext())
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"
        // TODO: Replace this query with Firebase 'trips' collection query where 'isPublic' == true
        val cursor = dbHelper.getPublicTrips() 

        val freshList = mutableListOf<Trip>()

        if (cursor != null && cursor.moveToFirst()) {
            do {
                val tripId = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val trip = Trip(
                    id = tripId,
                    title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
                    location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
                    description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
                    imageUri = cursor.getString(cursor.getColumnIndexOrThrow("cover_image")) ?: "",
                    isPublic = true,
                    authorName = cursor.getString(cursor.getColumnIndexOrThrow("author_name")) ?: "Traveler",
                    // --- FIX: Populate properties so DiffUtil detects real changes (e.g. Likes) ---
                    likeCount = dbHelper.getLikeCount(tripId),
                    commentCount = dbHelper.getCommentCount(tripId),
                    isLikedByMe = dbHelper.isTripLikedByUser(tripId, currentUserEmail)
                )
                freshList.add(trip)
            } while (cursor.moveToNext())
            cursor.close()
        }
        
        fullTripList.clear()
        fullTripList.addAll(freshList)
        adapter.updateData(ArrayList(fullTripList)) 
    }

    private fun filterList(query: String) {
        val filteredList = if (query.isEmpty()) {
            fullTripList
        } else {
            fullTripList.filter { trip ->
                trip.title.contains(query, ignoreCase = true) || trip.location.contains(query, ignoreCase = true)
            }
        }
        adapter.updateData(ArrayList(filteredList))
    }
}
