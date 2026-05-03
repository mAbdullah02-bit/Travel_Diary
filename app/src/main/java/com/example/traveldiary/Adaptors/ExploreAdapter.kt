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
    private val onTripClick: (Trip) -> Unit
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
        val dbHelper = DatabaseHelper(context)
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        holder.title.text = currentTrip.title
        holder.location.text = currentTrip.location
        holder.description.text = currentTrip.description
        holder.authorName.text = currentTrip.authorName
        holder.date.text = currentTrip.date
        
        // --- Social Data: Always fetch fresh state to prevent "double like" state issues ---
        val actualLikeCount = dbHelper.getLikeCount(currentTrip.id)
        val actualIsLiked = dbHelper.isTripLikedByUser(currentTrip.id, currentUserEmail)
        val actualCommentCount = dbHelper.getCommentCount(currentTrip.id)

        holder.likeCount.text = actualLikeCount.toString()
        holder.commentCount.text = actualCommentCount.toString()
        
        holder.likeIcon.setImageResource(if (actualIsLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        holder.likeIcon.setColorFilter(if (actualIsLiked) context.getColor(android.R.color.holo_red_dark) else context.getColor(R.color.gray))

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
            val currentPos = holder.bindingAdapterPosition
            if (currentPos != RecyclerView.NO_POSITION) {
                val trip = tripList[currentPos]
                
                // Toggle in Database
                dbHelper.toggleLike(trip.id, currentUserEmail)
                
                // --- FIX: Sync internal model so notifyItemChanged has correct metadata ---
                trip.isLikedByMe = dbHelper.isTripLikedByUser(trip.id, currentUserEmail)
                trip.likeCount = dbHelper.getLikeCount(trip.id)
                
                notifyItemChanged(currentPos)
                
                // TODO: Sync toggle to Firebase Firestore in the future
            }
        }
    }

    fun updateData(newList: List<Trip>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = tripList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean = tripList[oldPos].id == newList[newPos].id
            override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
                val old = tripList[oldPos]
                val next = newList[newPos]
                // Check all social fields that can change
                return old.likeCount == next.likeCount && 
                       old.isLikedByMe == next.isLikedByMe &&
                       old.commentCount == next.commentCount &&
                       old.title == next.title
            }
        }
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        // Store a NEW list instance to ensure calculateDiff works next time
        tripList = newList.map { it.copy() }
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = tripList.size
}
