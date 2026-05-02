package com.example.traveldiary.Adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.R
import com.example.traveldiary.models.ChatMessage

class ChatAdapter(private val messageList: MutableList<ChatMessage>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userContainer: LinearLayout = view.findViewById(R.id.user_message_container)
        val userText: TextView = view.findViewById(R.id.user_message_text)
        val aiContainer: LinearLayout = view.findViewById(R.id.ai_message_container)
        val aiText: TextView = view.findViewById(R.id.ai_message_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.aichatbot_icon, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messageList[position]

        if (chatMessage.isUser) {
            holder.userContainer.visibility = View.VISIBLE
            holder.aiContainer.visibility = View.GONE
            holder.userText.text = chatMessage.message
        } else {
            holder.userContainer.visibility = View.GONE
            holder.aiContainer.visibility = View.VISIBLE
            holder.aiText.text = chatMessage.message
        }
    }

    override fun getItemCount() = messageList.size
}