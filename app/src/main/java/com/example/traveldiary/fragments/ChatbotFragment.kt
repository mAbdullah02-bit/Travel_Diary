package com.example.traveldiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.traveldiary.DatabaseHelper
import com.example.traveldiary.R
import com.example.traveldiary.Adaptors.ChatAdapter
import com.example.traveldiary.models.ChatMessage
import com.example.traveldiary.models.ChatSession
import com.google.android.material.card.MaterialCardView
import com.google.ai.client.generativeai.GenerativeModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
class ChatbotFragment : Fragment() {

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.aichatbot_message, container, false)

        // Find views safely. Using <View> for the send button prevents casting crashes!
        val backBtn = view.findViewById<ImageView>(R.id.chat_back_btn)
        val inputBox = view.findViewById<EditText>(R.id.chat_input_box)
        val sendBtn = view.findViewById<View>(R.id.chat_send_btn)
        recyclerView = view.findViewById<RecyclerView>(R.id.chat_recycler_view)

        // Safety Check: If Android Studio can't find the XML elements, stop the crash!
        if (backBtn == null || inputBox == null || sendBtn == null || recyclerView == null) {
            android.util.Log.e("ChatbotError", "CRITICAL: Missing an ID in fragment_chatbot.xml!")
            android.widget.Toast.makeText(requireContext(), "Layout error: Check Logcat", android.widget.Toast.LENGTH_LONG).show()
            return view // Returns the blank view without crashing the app
        }

        // Setup RecyclerView using our Global ChatSession memory
        chatAdapter = ChatAdapter(ChatSession.messages)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = chatAdapter

        // Only show the greeting if this is a brand new conversation
        if (ChatSession.messages.isEmpty()) {
            addMessage("Hi there! I am your AI Travel Assistant. Ask me anything about your past trips or where you should go next!", false)
        }

        backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        sendBtn.setOnClickListener {
            val query = inputBox.text.toString().trim()
            if (query.isNotEmpty()) {
                inputBox.text.clear()
                addMessage(query, true)
                generateAIResponse(query)
            }
        }

        return view
    }

    private fun addMessage(text: String, isUser: Boolean) {
        ChatSession.messages.add(ChatMessage(text, isUser))
        chatAdapter.notifyItemInserted(ChatSession.messages.size - 1)
        recyclerView.smoothScrollToPosition(ChatSession.messages.size - 1)
    }

    private fun generateAIResponse(userPrompt: String) {
        val typingIndex = ChatSession.messages.size
        ChatSession.messages.add(ChatMessage("Typing...", false))
        chatAdapter.notifyItemInserted(typingIndex)
        recyclerView.smoothScrollToPosition(typingIndex)

        // Fetch User Data from SQLite
        val dbHelper = DatabaseHelper(requireContext())
        val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "guest"
        val cursor = dbHelper.getUserTrips(currentUserEmail)

        var travelHistory = ""
        if (cursor != null && cursor.moveToFirst()) {
            do {
                val title = cursor.getString(cursor.getColumnIndexOrThrow("title"))
                val location = cursor.getString(cursor.getColumnIndexOrThrow("location"))
                travelHistory += "- Went to $location ($title)\n"
            } while (cursor.moveToNext())
            cursor.close()
        }

        val systemInstruction = if (travelHistory.isNotEmpty()) {
            "You are a helpful travel assistant. The user's past trips are:\n$travelHistory\nUse this to personalize your advice if they ask about their travels."
        } else {
            "You are a helpful travel assistant."
        }

        val finalPrompt = "$systemInstruction\n\nUser Question: $userPrompt"


        //  THE TIMEOUT FIX
        val requestOpts = com.google.ai.client.generativeai.type.RequestOptions(timeout = 60.seconds)

        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = "AIzaSyAbsyZv0vGuVJ0ChC-KDxUJSsDRFhBqf7A",
            requestOptions = requestOpts
        )

        lifecycleScope.launch {
            try {
                val response = generativeModel.generateContent(finalPrompt)

                ChatSession.messages.removeAt(typingIndex)
                chatAdapter.notifyItemRemoved(typingIndex)

                addMessage(response.text ?: "I'm having trouble thinking right now.", false)
            } catch (e: Exception) {
                android.util.Log.e("Gemini Error", "Crash details: ", e)
                ChatSession.messages.removeAt(typingIndex)
                chatAdapter.notifyItemRemoved(typingIndex)

                // Clean FYP Error Handling!
                val rawError = e.message ?: ""
                val friendlyMessage = if (rawError.contains("503") || rawError.contains("high demand")) {
                    "Google's AI servers are currently very busy. Please wait a few seconds and try asking again!"
                } else {
                    "Hmm, I had trouble connecting to the network. Please try again."
                }

                addMessage(friendlyMessage, false)
            }
        }
    }
}

