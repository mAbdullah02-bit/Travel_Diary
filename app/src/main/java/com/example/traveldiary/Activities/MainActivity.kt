package com.example.traveldiary.Activities

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.traveldiary.R
import com.example.traveldiary.fragments.AddTripFragment
import com.example.traveldiary.fragments.ExploreFragment
import com.example.traveldiary.fragments.HomeFragment
import com.example.traveldiary.fragments.NotiFragment
import com.example.traveldiary.fragments.ProfileFragment

class MainActivity : AppCompatActivity() {
    private lateinit var navHome: ImageView
    private lateinit var navExplore: ImageView
    private lateinit var navAlerts: ImageView
    private lateinit var navProfile: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        // main activity
        setContentView(R.layout.activity_main)
        // Catch the Intent data passed from LoginActivity
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "Unknown User"
        val isGuest = intent.getBooleanExtra("IS_GUEST", true)

        println("SUCCESS: Logged in as $userEmail. Guest Mode: $isGuest")


        navHome = findViewById<ImageView>(R.id.nav_home)
        navExplore = findViewById<ImageView>(R.id.nav_explore)
        navAlerts = findViewById<ImageView>(R.id.nav_alerts)
        navProfile = findViewById<ImageView>(R.id.nav_profile)
        val navAddTrip = findViewById<ImageView>(R.id.nav_add_trip)


        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            updateNavColors(navHome)
        }

        // 4. Wire up the buttons to change the channels!
        navHome.setOnClickListener {
            loadFragment(HomeFragment())
            updateNavColors(navHome)
        }
        navExplore.setOnClickListener {
            loadFragment(ExploreFragment())
            updateNavColors(navExplore)
        }
        navAlerts.setOnClickListener {
            loadFragment(NotiFragment())
            updateNavColors(navAlerts)
        }
        navProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            updateNavColors(navProfile)
        }

        // (We will wire up the + button later to open your New Trip screen!)
        navAddTrip.setOnClickListener { loadFragment(AddTripFragment()) }
    }

    // This is the engine that does the actual swapping (Satisfies Requirement F4)
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Swaps out whatever is currently in the box
            .commit()
    }
    private fun updateNavColors(selectedIcon: ImageView) {
        // Grab the actual color values from your res/values/colors.xml file
        val grayColor = ContextCompat.getColor(this, R.color.gray)
        val blueColor = ContextCompat.getColor(this, R.color.blue)

        // Reset ALL icons to gray first
        navHome.setColorFilter(grayColor)
        navExplore.setColorFilter(grayColor)
        navAlerts.setColorFilter(grayColor)
        navProfile.setColorFilter(grayColor)

        // Paint ONLY the selected icon blue
        selectedIcon.setColorFilter(blueColor)
    }
}