package com.example.traveldiary.utils

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageHelper {
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadTripImage(uri: Uri, tripId: String): String {
        val ref = storage.reference.child("trips/$tripId/cover.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun uploadProfileImage(uri: Uri, email: String): String {
        val ref = storage.reference.child("users/$email/profile.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}
