package com.example.traveldiary.models

import java.io.Serializable

data class User(
    val email: String = "",
    val name: String = "",
    val bio: String = "",
    val coverImage: String = "",
    val dateJoined: Long = System.currentTimeMillis()
) : Serializable
