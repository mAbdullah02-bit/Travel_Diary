package com.example.traveldiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.traveldiary.R // importing R class

class AddTripFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // inflating home_layout into this fragment
        return inflater.inflate(R.layout.activity_addtrip, container, false)
    }
}