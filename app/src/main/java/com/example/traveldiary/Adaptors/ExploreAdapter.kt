package com.example.traveldiary.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

class ExploreAdapter(
    private var tripList: List<Trip>,
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    class ExploreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // These match the IDs we just added to item_explore_card.xml!
        val title: TextView = view.findViewById(R.id.explore_card_title)
        val location: TextView = view.findViewById(R.id.explore_card_location)
        val description: TextView = view.findViewById(R.id.explore_card_desc)
        val image: ImageView = view.findViewById(R.id.explore_card_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_card, parent, false) // Uses the BIG card!
        return ExploreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val currentTrip = tripList[position]

        holder.title.text = currentTrip.title
        holder.location.text = currentTrip.location
        holder.description.text = currentTrip.description
        holder.image.setImageResource(currentTrip.imageResId)

        // Make the whole big card clickable
        holder.itemView.setOnClickListener {
            onTripClick(currentTrip)
        }
    }

    fun updateData(newList: List<Trip>) {
        tripList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return tripList.size
    }
}