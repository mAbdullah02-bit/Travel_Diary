package com.example.traveldiary.fragments

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
import com.example.traveldiary.R
import com.example.traveldiary.models.Trip

// 1. A quick Data Model for Comments
data class Comment(val author: String, val text: String)

        // 2. A quick Adapter for Comments
        class CommentAdapter(private val comments: MutableList<Comment>) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {
        class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val authorName: TextView = view.findViewById(R.id.comment_author)
    val commentText: TextView = view.findViewById(R.id.comment_text)
}
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
    return CommentViewHolder(view)
}
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
    holder.authorName.text = comments[position].author
    holder.commentText.text = comments[position].text
}
        override fun getItemCount() = comments.size
}

        // 3. The actual Fragment!
        class ExploreDetailFragment : Fragment() {

    private val fakeComments = mutableListOf(
            Comment("Sarah Johnson", "Wow, this looks like an amazing trip!"),
            Comment("David Chen", "Did you find it expensive to travel there?"),
            Comment("Emily R.", "Added to my bucket list! 😍")
    )
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
            val view = inflater.inflate(R.layout.explore_detail, container, false)

            // Unpack the trip data
            val trip = arguments?.getSerializable("TRIP_DATA") as? Trip

    if (trip != null) {
        view.findViewById<TextView>(R.id.explore_detail_title).text = trip.title
        view.findViewById<TextView>(R.id.explore_detail_location).text = trip.location
        view.findViewById<TextView>(R.id.explore_detail_desc).text = trip.description
        view.findViewById<ImageView>(R.id.explore_detail_image).setImageResource(trip.imageResId)
    }

    // Setup Back Button
    view.findViewById<ImageView>(R.id.explore_detail_back).setOnClickListener {
        parentFragmentManager.popBackStack()
    }

    // Setup Comments RecyclerView
    val recycler = view.findViewById<RecyclerView>(R.id.explore_comments_recycler)
            recycler.layoutManager = LinearLayoutManager(requireContext())
    commentAdapter = CommentAdapter(fakeComments)
    recycler.adapter = commentAdapter

    // Setup the "Send Comment" Button
    val inputComment = view.findViewById<EditText>(R.id.input_comment)
            val btnSend = view.findViewById<ImageView>(R.id.btn_send_comment)

            btnSend.setOnClickListener {
        val text = inputComment.text.toString()
        if (text.isNotEmpty()) {
            // Add the new comment to the list
            fakeComments.add(Comment("You", text))
            commentAdapter.notifyItemInserted(fakeComments.size - 1) // Tell UI to redraw

            // Scroll to the bottom to see it!
            recycler.scrollToPosition(fakeComments.size - 1)

            // Clear the text box
            inputComment.text.clear()
        } else {
            Toast.makeText(requireContext(), "Comment cannot be empty", Toast.LENGTH_SHORT).show()
        }
    }

    return view
    }
}