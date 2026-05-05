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

class ExploreAdapter(
    private val onTripClick: (Trip) -> Unit,
    private val onLikeClick: (Trip) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    private var tripList: List<Trip> = emptyList()

    class ExploreViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.explore_card_title)
        val location: TextView = view.findViewById(R.id.explore_card_location)
        val description: TextView = view.findViewById(R.id.explore_card_desc)
        val image: ImageView = view.findViewById(R.id.explore_card_image)
        val authorName: TextView = view.findViewById(R.id.explore_card_user_name)
        val date: TextView = view.findViewById(R.id.explore_card_date)
        val likeIcon: ImageView = view.findViewById(R.id.explore_card_like_icon)
        val likeCount: TextView = view.findViewById(R.id.explore_card_like_count)
        val commentCount: TextView = view.findViewById(R.id.explore_card_comment_count)
        val avatarText: TextView = view.findViewById(R.id.explore_card_avatar_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_card, parent, false)
        return ExploreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val currentTrip = tripList[position]
        val context = holder.itemView.context

        holder.title.text = currentTrip.title
        holder.location.text = currentTrip.location
        holder.description.text = currentTrip.description
        holder.authorName.text = currentTrip.authorName
        holder.date.text = currentTrip.date

        holder.likeCount.text = currentTrip.likeCount.toString()
        holder.commentCount.text = currentTrip.commentCount.toString()

        holder.likeIcon.setImageResource(if (currentTrip.isLikedByMe) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        holder.likeIcon.setColorFilter(if (currentTrip.isLikedByMe) context.getColor(android.R.color.holo_red_dark) else context.getColor(R.color.gray))

        holder.avatarText.text = if (currentTrip.authorName.isNotEmpty()) currentTrip.authorName.take(1).uppercase() else "?"

        val placeholder = ColorDrawable(Color.parseColor("#D3D3D3"))

        holder.likeIcon.setOnClickListener {
            // THE FIX: Strict lock to prevent rapid clicking
            holder.likeIcon.isEnabled = false

            val isNowLiked = !currentTrip.isLikedByMe
            currentTrip.isLikedByMe = isNowLiked

            // Safe local math floor
            currentTrip.likeCount += if (isNowLiked) 1 else -1
            if (currentTrip.likeCount < 0) currentTrip.likeCount = 0

            holder.likeIcon.setImageResource(if (isNowLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            holder.likeIcon.setColorFilter(if (isNowLiked) context.getColor(android.R.color.holo_red_dark) else context.getColor(R.color.gray))
            holder.likeCount.text = currentTrip.likeCount.toString()

            onLikeClick(currentTrip)

            // Unlock after 1 full second
            holder.likeIcon.postDelayed({ holder.likeIcon.isEnabled = true }, 1000)
        }

        holder.itemView.setOnClickListener {
            onTripClick(currentTrip)
        }

        val imageSource = when {
            currentTrip.imageUrl.isNotEmpty() -> currentTrip.imageUrl
            currentTrip.imageUri.isNotEmpty() -> currentTrip.imageUri
            else -> null
        }

        Glide.with(context)
            .load(imageSource)
            .placeholder(placeholder)
            .error(placeholder)
            .into(holder.image)
    }

    fun updateData(newList: List<Trip>) {
        this.tripList = newList.toList()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = tripList.size
}