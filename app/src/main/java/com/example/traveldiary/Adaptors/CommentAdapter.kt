package com.example.traveldiary.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.R
import com.example.traveldiary.models.Comment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommentAdapter(private var commentList: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.comment_user_name)
        val commentText: TextView = view.findViewById(R.id.comment_text)
        val dateText: TextView = view.findViewById(R.id.comment_date)
        val avatarText: TextView = view.findViewById(R.id.comment_avatar_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.userName.text = comment.userName
        holder.commentText.text = comment.text
        
        // Simple time formatting
        try {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.dateText.text = sdf.format(Date(comment.date.toLong()))
        } catch (e: Exception) {
            holder.dateText.text = "Just now"
        }

        holder.avatarText.text = if (comment.userName.isNotEmpty()) comment.userName.take(1).uppercase() else "U"
    }

    override fun getItemCount(): Int = commentList.size

    fun updateData(newList: List<Comment>) {
        // .toList() creates a fresh copy so the Recycler updates instantly
        this.commentList = newList.toList()
        notifyDataSetChanged()
    }
}