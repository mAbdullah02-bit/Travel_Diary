package com.example.traveldiary.Adaptors

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class TripAdapter(
    private var tripList: List<Trip>, // Fix 2: Changed to private var List<Trip> (Requirement F2)
    private val onTripClick: (Trip) -> Unit
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

        // --- UI Polish: Glide with grayish placeholder (Requirement F4) ---
        val placeholder = ColorDrawable(Color.parseColor("#D3D3D3"))
        
        val imageSource = when {
            currentTrip.imageUrl.isNotEmpty() -> currentTrip.imageUrl
            currentTrip.imageUri.isNotEmpty() -> currentTrip.imageUri
            else -> null
        }

        Glide.with(holder.itemView.context)
            .load(imageSource)
            .placeholder(placeholder)
            .error(placeholder)
            .into(holder.tripImage)

        holder.tripVisibility.text = if (currentTrip.isPublic) "Public" else "Private"

        holder.itemView.setOnClickListener {
            onTripClick(currentTrip)
        }
    }

    // Fix 2: Simplified updateData to prevent destroying search filter data
    fun updateData(newList: List<Trip>) {
        this.tripList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tripList.size
}
