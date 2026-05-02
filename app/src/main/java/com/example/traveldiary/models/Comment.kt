package com.example.traveldiary.models

data class Comment(
    val id: Int,
    val tripId: Int,
    val userEmail: String,
    val userName: String,
    val text: String,
    val date: String
)