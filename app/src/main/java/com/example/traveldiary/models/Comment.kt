package com.example.traveldiary.models

import java.io.Serializable

data class Comment(
    val id: Int = 0,
    val commentId: String = "",
    val tripId: String = "",
    val userEmail: String = "",
    val userName: String = "Anonymous",
    val text: String = "",
    val date: String = System.currentTimeMillis().toString()
) : Serializable
