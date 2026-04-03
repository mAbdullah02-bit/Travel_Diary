package com.example.traveldiary.models

import java.io.Serializable

data class Trip(
        val title:String,

        val location:String,

        val date:String,
        val description:String,
        val imageResId:Int,
        val rating:Double


) :Serializable
