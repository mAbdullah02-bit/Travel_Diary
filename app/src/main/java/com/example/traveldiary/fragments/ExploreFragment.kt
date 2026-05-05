package com.example.traveldiary.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.Adaptors.ExploreAdapter
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.example.traveldiary.utils.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExploreFragment : Fragment() {

    private lateinit var adapter: ExploreAdapter
    private var fullTripList = mutableListOf<Trip>()
    private val firestoreHelper = FirestoreHelper()
    private var userLikedTripIds = setOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_explore, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.explore_recycler_view)

        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        adapter = ExploreAdapter(
            onTripClick = { clickedTrip: Trip ->
                val detailFragment = ExploreDetailFragment()
                val bundle = Bundle()
                bundle.putSerializable("TRIP_DATA", clickedTrip)
                detailFragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            },
            onLikeClick = { clickedTrip: Trip ->
                if (clickedTrip.tripId.isNotEmpty()) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            firestoreHelper.toggleLike(clickedTrip.tripId, currentUserEmail)
                        } catch (e: Exception) {
                            // Ignore errors quietly on the feed
                        }
                    }
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // THE FIX: Catch the state passed back from Detail Page instantly!
        parentFragmentManager.setFragmentResultListener("like_status_update", viewLifecycleOwner) { _, bundle ->
            val updatedTripId = bundle.getString("TRIP_ID") ?: return@setFragmentResultListener
            val updatedIsLiked = bundle.getBoolean("IS_LIKED")
            val updatedLikeCount = bundle.getInt("LIKE_COUNT")

            // Find the item we just edited
            val tripIndex = fullTripList.indexOfFirst { it.tripId == updatedTripId }

            if (tripIndex != -1) {
                // Update the memory array
                fullTripList[tripIndex].isLikedByMe = updatedIsLiked
                fullTripList[tripIndex].likeCount = updatedLikeCount

                // Crucial: Update our local likes set so the search filter doesn't erase it
                val mutableLikedIds = userLikedTripIds.toMutableSet()
                if (updatedIsLiked) {
                    mutableLikedIds.add(updatedTripId)
                } else {
                    mutableLikedIds.remove(updatedTripId)
                }
                userLikedTripIds = mutableLikedIds

                // Force the adapter to visually repaint ONLY this one specific card
                adapter.notifyItemChanged(tripIndex)
            }
        }

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyLikesAndFilter()
            }
        })

        setupFirestoreListener()
        return view
    }

    private fun setupFirestoreListener() {
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: ""

        // 1. Listen for Public Trips
        firestoreHelper.getPublicTripsListener { trips ->
            lifecycleScope.launch(Dispatchers.Main) {
                fullTripList.clear()
                fullTripList.addAll(trips)
                applyLikesAndFilter()
            }
        }

        // 2. Listen for User Likes
        if (currentUserEmail.isNotEmpty()) {
            firestoreHelper.getUserLikesListener(currentUserEmail) { likedIds ->
                lifecycleScope.launch(Dispatchers.Main) {
                    userLikedTripIds = likedIds
                    applyLikesAndFilter()
                }
            }
        }
    }

    private fun applyLikesAndFilter() {
        // Sync pure Firebase truth
        fullTripList.forEach { trip ->
            trip.isLikedByMe = userLikedTripIds.contains(trip.tripId)
        }

        val searchBar = view?.findViewById<EditText>(R.id.search_bar)
        val query = searchBar?.text?.toString() ?: ""

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