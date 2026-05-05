package com.example.traveldiary.models

import com.google.firebase.firestore.PropertyName
import java.io.Serializable

data class Trip(
    var id: Int = 0, // Local SQLite ID
    var tripId: String = "", // Firestore Document ID
    var userEmail: String = "",
    var title: String = "",
    var description: String = "",
    var location: String = "",
    var date: String = "",
    var imageUrl: String = "",
    var imageUri: String = "",
    var imageResId: Int = 0,
    
    @get:PropertyName("public")
    @set:PropertyName("public")
    var isPublic: Boolean = false,
    
    var authorName: String = "Traveler",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    
    @get:PropertyName("isLikedByMe")
    @set:PropertyName("isLikedByMe")
    var isLikedByMe: Boolean = false
) : Serializable
