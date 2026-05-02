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

class TripAdapter(private val tripList: MutableList<Trip>,private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {
    class TripViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tripTitle: TextView = view.findViewById(R.id.card_title)
        val tripLocation: TextView = view.findViewById(R.id.card_location)
        val tripImage: ImageView = view.findViewById(R.id.card_image)
        val tripVisibility: TextView = view.findViewById(R.id.card_visiblity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.trip_card, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val currentTrip = tripList[position]

        holder.tripTitle.text = currentTrip.title
        holder.tripLocation.text = currentTrip.location

        // --- FIX 1: Handle Database Images ---
        if (currentTrip.imageUri.isNotEmpty()) {
            // If there's a URI from the gallery/camera, use it
            try {
                holder.tripImage.setImageURI(android.net.Uri.parse(currentTrip.imageUri))
            } catch (e: SecurityException) {
                holder.tripImage.setImageResource(currentTrip.imageResId)
            }
        } else {
            // Otherwise, use the default icon
            holder.tripImage.setImageResource(currentTrip.imageResId)
        }

        // --- FIX 2: Show Visibility ---
        holder.tripVisibility.text = if (currentTrip.isPublic) "Public" else "Private"

        holder.itemView.setOnClickListener {
            onTripClick(currentTrip)
        }
    }

    fun updateData(newList: List<Trip>) {
        // --- CRITICAL FIX: Prevent clearing if it's the same list object ---
        if (this.tripList !== newList) {
            this.tripList.clear()
            this.tripList.addAll(newList)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}