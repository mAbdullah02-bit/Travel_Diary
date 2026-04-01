package com.example.traveldiary.Activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.traveldiary.R
import com.example.traveldiary.fragments.AddTripFragment
import com.example.traveldiary.fragments.ExploreFragment
import com.example.traveldiary.fragments.HomeFragment
import com.example.traveldiary.fragments.NotiFragment
import com.example.traveldiary.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // main activity
        setContentView(R.layout.activity_main)

        // 2. Find the physical buttons on your TV frame using the IDs we set earlier
        val navHome = findViewById<ImageView>(R.id.nav_home)
        val navExplore = findViewById<ImageView>(R.id.nav_explore)
        val navAlerts = findViewById<ImageView>(R.id.nav_alerts)
        val navProfile = findViewById<ImageView>(R.id.nav_profile)
        val navAddTrip = findViewById<ImageView>(R.id.nav_add_trip) // The floating + button

        // 3. Turn the TV on: Load the Home screen by default when the app opens
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 4. Wire up the buttons to change the channels!
        navHome.setOnClickListener { loadFragment(HomeFragment()) }
        navExplore.setOnClickListener { loadFragment(ExploreFragment()) }
        navAlerts.setOnClickListener { loadFragment(NotiFragment()) }
        navProfile.setOnClickListener { loadFragment(ProfileFragment()) }

        // (We will wire up the + button later to open your New Trip screen!)
        navAddTrip.setOnClickListener { loadFragment(AddTripFragment()) }
    }

    // This is the engine that does the actual swapping (Satisfies Requirement F4)
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Swaps out whatever is currently in the box
            .commit()
    }
}