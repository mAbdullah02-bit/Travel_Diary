package com.example.traveldiary.Adaptors

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip
import com.google.firebase.auth.FirebaseAuth

class ExploreAdapter(
    private var tripList: List<Trip>,
    private val onTripClick: (Trip) -> Unit
) : RecyclerView.Adapter<ExploreAdapter.ExploreViewHolder>() {

    init {
        setHasStableIds(true)
    }

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

    override fun getItemId(position: Int): Long = tripList[position].id.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_explore_card, parent, false)
        return ExploreViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExploreViewHolder, position: Int) {
        val currentTrip = tripList[position]
        val context = holder.itemView.context
        val dbHelper = DatabaseHelper(context)
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        holder.title.text = currentTrip.title
        holder.location.text = currentTrip.location
        holder.description.text = currentTrip.description
        holder.authorName.text = currentTrip.authorName
        holder.date.text = currentTrip.date
        
        // --- TODO: FUTURE FIREBASE MIGRATION ---
        // Fetch like/comment counts from Firebase Firestore collections instead of SQLite
        holder.likeCount.text = dbHelper.getLikeCount(currentTrip.id).toString()
        holder.commentCount.text = dbHelper.getCommentCount(currentTrip.id).toString()
        
        val isLiked = dbHelper.isTripLikedByUser(currentTrip.id, currentUserEmail)
        holder.likeIcon.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        holder.likeIcon.setColorFilter(if (isLiked) context.getColor(android.R.color.holo_red_dark) else context.getColor(R.color.gray))

        holder.avatarText.text = if (currentTrip.authorName.isNotEmpty()) currentTrip.authorName.take(1).uppercase() else "?"

        if (currentTrip.imageUri.isNotEmpty()) {
            try {
                holder.image.setImageURI(Uri.parse(currentTrip.imageUri))
            } catch (e: SecurityException) {
                holder.image.setImageResource(currentTrip.imageResId)
            }
        } else {
            holder.image.setImageResource(currentTrip.imageResId)
        }

        holder.itemView.setOnClickListener { onTripClick(currentTrip) }

        holder.likeIcon.setOnClickListener {
            // --- TODO: FUTURE FIREBASE MIGRATION ---
            // Toggle like status in Firebase Firestore 'likes' collection
            dbHelper.toggleLike(currentTrip.id, currentUserEmail)
            notifyItemChanged(holder.bindingAdapterPosition)
        }
    }

    fun updateData(newList: List<Trip>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = tripList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = tripList[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean = tripList[oldPos] == newList[newPos]
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        tripList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = tripList.size
}
