package com.example.traveldiary.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.Adaptors.CommentAdapter
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.models.Comment
import com.example.traveldiary.models.Trip
import com.google.firebase.auth.FirebaseAuth

class ExploreDetailFragment : Fragment() {

    private lateinit var commentAdapter: CommentAdapter
    private var commentList = mutableListOf<Comment>()
    private var currentTrip: Trip? = null
    private lateinit var likeIcon: ImageView
    private lateinit var likeCountText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.explore_detail, container, false)

        currentTrip = arguments?.getSerializable("TRIP_DATA") as? Trip

        val backButton = view.findViewById<ImageView>(R.id.explore_detail_back)
        val titleView = view.findViewById<TextView>(R.id.explore_detail_title)
        val locationView = view.findViewById<TextView>(R.id.explore_detail_location)
        val descView = view.findViewById<TextView>(R.id.explore_detail_desc)
        val imageView = view.findViewById<ImageView>(R.id.explore_detail_image)
        val userNameView = view.findViewById<TextView>(R.id.explore_detail_user_name)
        val dateView = view.findViewById<TextView>(R.id.explore_detail_date)
        val avatarText = view.findViewById<TextView>(R.id.explore_detail_avatar_text)
        
        likeIcon = view.findViewById(R.id.explore_detail_like_icon)
        likeCountText = view.findViewById(R.id.explore_detail_like_count)
        val commentCountText = view.findViewById<TextView>(R.id.explore_detail_comment_count)

        val commentRecyclerView = view.findViewById<RecyclerView>(R.id.explore_comments_recycler)
        val inputComment = view.findViewById<EditText>(R.id.input_comment)
        val btnSendComment = view.findViewById<ImageView>(R.id.btn_send_comment)

        val dbHelper = DatabaseHelper(requireContext())
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest@example.com"

        currentTrip?.let { trip ->
            titleView.text = trip.title
            locationView.text = trip.location
            descView.text = trip.description
            userNameView.text = trip.authorName
            dateView.text = trip.date
            avatarText.text = if (trip.authorName.isNotEmpty()) trip.authorName.take(1).uppercase() else "?"

            if (trip.imageUri.isNotEmpty()) {
                try {
                    imageView.setImageURI(Uri.parse(trip.imageUri))
                } catch (e: SecurityException) {
                    imageView.setImageResource(trip.imageResId)
                }
            } else {
                imageView.setImageResource(trip.imageResId)
            }

            updateLikeUI(dbHelper, currentUserEmail)
            updateCommentCount(trip.id, commentCountText)

            // --- LIKE CLICK FUNCTIONALITY ---
            likeIcon.setOnClickListener {
                dbHelper.toggleLike(trip.id, currentUserEmail)
                updateLikeUI(dbHelper, currentUserEmail)
                // TODO: Sync like status to Firebase in the future
            }
        }

        backButton.setOnClickListener { parentFragmentManager.popBackStack() }

        // Setup Comments
        commentAdapter = CommentAdapter(commentList)
        commentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentRecyclerView.adapter = commentAdapter
        
        loadComments()

        btnSendComment.setOnClickListener {
            val text = inputComment.text.toString().trim()
            if (text.isNotEmpty()) {
                currentTrip?.let { trip ->
                    dbHelper.addComment(trip.id, currentUserEmail, text)
                    inputComment.setText("")
                    loadComments()
                    updateCommentCount(trip.id, commentCountText)
                    Toast.makeText(requireContext(), "Comment posted!", Toast.LENGTH_SHORT).show()
                    // TODO: Sync comments to Firebase in the future
                }
            }
        }

        return view
    }

    private fun updateLikeUI(dbHelper: DatabaseHelper, email: String) {
        currentTrip?.let { trip ->
            val isLiked = dbHelper.isTripLikedByUser(trip.id, email)
            val count = dbHelper.getLikeCount(trip.id)
            
            likeIcon.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
            likeIcon.setColorFilter(if (isLiked) requireContext().getColor(android.R.color.holo_red_dark) else requireContext().getColor(R.color.gray))
            likeCountText.text = "$count Likes"
        }
    }

    private fun loadComments() {
        val dbHelper = DatabaseHelper(requireContext())
        currentTrip?.let { trip ->
            val cursor = dbHelper.getCommentsForTrip(trip.id)
            commentList.clear()
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    val comment = Comment(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        tripId = cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                        userEmail = cursor.getString(cursor.getColumnIndexOrThrow("user_email")),
                        userName = cursor.getString(cursor.getColumnIndexOrThrow("name")) ?: "Anonymous",
                        text = cursor.getString(cursor.getColumnIndexOrThrow("comment_text")),
                        date = cursor.getString(cursor.getColumnIndexOrThrow("comment_date"))
                    )
                    commentList.add(comment)
                } while (cursor.moveToNext())
                cursor.close()
            }
            commentAdapter.updateData(commentList)
        }
    }

    private fun updateCommentCount(tripId: Int, textView: TextView) {
        val dbHelper = DatabaseHelper(requireContext())
        textView.text = "${dbHelper.getCommentCount(tripId)} Comments"
    }
}
