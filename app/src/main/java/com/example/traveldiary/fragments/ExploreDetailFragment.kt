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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.Adaptors.CommentAdapter
import com.example.traveldiary.R
import com.example.traveldiary.models.Comment
import com.example.traveldiary.models.Trip
import com.example.traveldiary.utils.FirestoreHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExploreDetailFragment : Fragment() {

    private lateinit var commentAdapter: CommentAdapter
    private var commentList = mutableListOf<Comment>()
    private var currentTrip: Trip? = null

    private lateinit var likeIcon: ImageView
    private lateinit var likeCountText: TextView
    private lateinit var commentCountText: TextView

    private val firestoreHelper = FirestoreHelper()
    private val auth = FirebaseAuth.getInstance()

    // Local State
    private var isLikedLocal = false
    private var localLikeCount = 0
    private var isProcessingLike = false // Spam prevention lock

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
        commentCountText = view.findViewById(R.id.explore_detail_comment_count)

        val commentRecyclerView = view.findViewById<RecyclerView>(R.id.explore_comments_recycler)
        val inputComment = view.findViewById<EditText>(R.id.input_comment)
        val btnSendComment = view.findViewById<ImageView>(R.id.btn_send_comment)

        val currentUserEmail = auth.currentUser?.email ?: "guest@example.com"
        val currentUserName = auth.currentUser?.displayName ?: "Anonymous"

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

            localLikeCount = trip.likeCount.toInt()
            isLikedLocal = trip.isLikedByMe
            commentCountText.text = "${trip.commentCount} Comments"

            updateLikeVisuals()

            likeIcon.setOnClickListener {
                if (trip.tripId.isNotEmpty() && !isProcessingLike) {
                    isProcessingLike = true // Lock the button

                    isLikedLocal = !isLikedLocal
                    localLikeCount += if (isLikedLocal) 1 else -1
                    if (localLikeCount < 0) localLikeCount = 0

                    updateLikeVisuals()

                    lifecycleScope.launch(Dispatchers.IO) {
                        try {
                            firestoreHelper.setLikeState(trip.tripId, currentUserEmail,isLikedLocal)
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                // Revert on failure
                                isLikedLocal = !isLikedLocal
                                localLikeCount += if (isLikedLocal) 1 else -1
                                if (localLikeCount < 0) localLikeCount = 0
                                updateLikeVisuals()
                                Toast.makeText(requireContext(), "Sync error", Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            isProcessingLike = false // Unlock
                        }
                    }
                }
            }
        }

        // THE FIX: Pass state back locally before closing!
        backButton.setOnClickListener {
            currentTrip?.let { trip ->
                val resultBundle = Bundle().apply {
                    putString("TRIP_ID", trip.tripId)
                    putBoolean("IS_LIKED", isLikedLocal)
                    putInt("LIKE_COUNT", localLikeCount)
                }
                parentFragmentManager.setFragmentResult("like_status_update", resultBundle)
            }
            parentFragmentManager.popBackStack()
        }

        commentAdapter = CommentAdapter(commentList)
        commentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        commentRecyclerView.adapter = commentAdapter
        setupCommentsListener()

        btnSendComment.setOnClickListener {
            val text = inputComment.text.toString().trim()
            if (text.isNotEmpty()) {
                currentTrip?.let { trip ->
                    if (trip.tripId.isNotEmpty()) {
                        inputComment.setText("")
                        lifecycleScope.launch(Dispatchers.IO) {
                            try {
                                firestoreHelper.addComment(trip.tripId, currentUserEmail, currentUserName, text)
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        }
        return view
    }

    private fun updateLikeVisuals() {
        likeIcon.setImageResource(if (isLikedLocal) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        likeIcon.setColorFilter(if (isLikedLocal) requireContext().getColor(android.R.color.holo_red_dark) else requireContext().getColor(R.color.gray))
        likeCountText.text = "$localLikeCount Likes"
    }

    private fun setupCommentsListener() {
        currentTrip?.let { trip ->
            if (trip.tripId.isNotEmpty()) {
                firestoreHelper.getCommentsListener(trip.tripId) { comments ->
                    lifecycleScope.launch(Dispatchers.Main) {
                        commentList.clear()
                        commentList.addAll(comments)
                        commentAdapter.updateData(commentList)
                        commentCountText.text = "${comments.size} Comments"
                    }
                }
            }
        }
    }
}