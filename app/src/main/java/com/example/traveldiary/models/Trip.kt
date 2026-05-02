package com.example.traveldiary.models

import java.io.Serializable
import com.example.traveldiary.R

data class Trip(
    var title: String,
    var location: String,
    var date: String,
    var description: String,
    var imageResId: Int = R.drawable.ic_image_replacer_foreground,
    var rating: Double = 0.0,
    var id: Int = -1,
    var imageUri: String = "",
    var isPublic: Boolean = false,
    var authorName: String = "Unknown User",
    var likeCount: Int = 0,
    var commentCount: Int = 0,
    var isLikedByMe: Boolean = false
) : Serializable
