package com.example.traveldiary.models

// An "object" in Kotlin is a Singleton. There is only ever ONE instance of this in memory.
object ChatSession {
    val messages = mutableListOf<ChatMessage>()
}