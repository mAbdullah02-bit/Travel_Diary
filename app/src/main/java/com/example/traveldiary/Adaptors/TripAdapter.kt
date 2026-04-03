package com.example.traveldiary.Adaptors
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class TripAdapter (

    private var tripList: List<Trip>,
    private val onTripClick: (Trip) -> Unit
): RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tripTitle: TextView = view.findViewById(R.id.card_title) // Check your actual XML ID
        val tripLocation: TextView = view.findViewById(R.id.card_location)
        val tripImage: ImageView = view.findViewById(R.id.card_image)
        val tripVisibility: TextView = view.findViewById(R.id.card_visiblity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_card, parent, false) // Make sure you have item_trip_card.xml!
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = tripList[position]

        holder.tripTitle.text = currentTrip.title
        holder.tripLocation.text = currentTrip.location
        holder.tripImage.setImageResource(currentTrip.imageResId)
        // When the card is clicked, pass the whole Trip object out to the Fragment!
        holder.itemView.setOnClickListener {
            onTripClick(currentTrip)
        }
    }
    fun updateData(newList: List<Trip>) {
        tripList = newList
        notifyDataSetChanged() // Tells the RecyclerView to redraw the screen
    }
    override fun getItemCount(): Int {
        return tripList.size
    }
}