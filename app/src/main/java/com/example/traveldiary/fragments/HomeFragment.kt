package com.example.traveldiary.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.R
import com.example.traveldiary.Adaptors.TripAdapter
import com.example.traveldiary.components.GuestDashboard
import com.example.traveldiary.components.TripStatsDashboard
import com.example.traveldiary.models.Trip
import com.example.traveldiary.utils.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth
import com.example.traveldiary.Activities.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var adapter: TripAdapter
    private var homeTrips = mutableListOf<Trip>()
    private val firestoreHelper = FirestoreHelper()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_home, container, false)
        val searchBar = view.findViewById<EditText>(R.id.search_bar)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler_view)
        val composeView = view.findViewById<ComposeView>(R.id.compose_view_stats)
        val timelineTitle = view.findViewById<TextView>(R.id.home_timeline)

        // Requirement F4: Ensure ComposeView handles lifecycle correctly
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        adapter = TripAdapter(homeTrips) { clickedTrip ->
            val detailFragment = TripDetailFragment()
            val bundle = Bundle()
            bundle.putSerializable("TRIP_DATA", clickedTrip)
            detailFragment.arguments = bundle

            // Fix 1: Use .replace() to prevent fragment overlap (Requirement F2)
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

        val fabAi = view.findViewById<View>(R.id.fab_ai_chat)
        fabAi.setOnClickListener {
            // Fix 1: Use .replace() for Chatbot navigation
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ChatbotFragment())
                .addToBackStack(null)
                .commit()
        }

        val currentUser = auth.currentUser
        // Fix 2: Home Page Logic - Real-time sync for private trips (Requirement F2)
        if (currentUser != null && !currentUser.isAnonymous) {
            recyclerView.visibility = View.VISIBLE
            searchBar.visibility = View.VISIBLE
            timelineTitle.visibility = View.VISIBLE
            setupFirestoreListener(currentUser.email ?: "")
        } else {
            // Guest mode: Show Guest Dashboard (Requirement F4)
            recyclerView.visibility = View.GONE
            searchBar.visibility = View.GONE
            timelineTitle.visibility = View.GONE
            
            composeView.setContent {
                GuestDashboard(onSignUpClick = {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                })
            }
        }

        return view
    }

    private fun setupFirestoreListener(email: String) {
        // Requirement F2: SnapshotListener for real-time private sync
        firestoreHelper.getUserTripsListener(email) { trips ->
            // Requirement F2: Update UI on Main thread
            lifecycleScope.launch(Dispatchers.Main) {
                homeTrips.clear()
                homeTrips.addAll(trips)
                adapter.updateData(homeTrips)
                updateComposeStats(trips)
            }
        }
    }

    private fun updateComposeStats(trips: List<Trip>) {
        val composeView = view?.findViewById<ComposeView>(R.id.compose_view_stats) ?: return
        val recentLoc = if (trips.isNotEmpty()) trips[0].location else "None"
        
        composeView.setContent {
            TripStatsDashboard(
                totalTrips = trips.size,
                recentLocation = recentLoc
            )
        }
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
}
