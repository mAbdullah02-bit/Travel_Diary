package com.example.traveldiary.utils

import com.example.traveldiary.models.Comment
import com.example.traveldiary.models.Like
import com.example.traveldiary.models.Trip
import com.example.traveldiary.models.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FirestoreHelper {
    private val db = FirebaseFirestore.getInstance()

    // --- Users ---
    suspend fun saveUser(user: User) = withContext(Dispatchers.IO) {
        db.collection("users").document(user.email).set(user, SetOptions.merge()).await()
    }

    suspend fun getUser(email: String): User? = withContext(Dispatchers.IO) {
        val doc = db.collection("users").document(email).get().await()
        doc.toObject(User::class.java)
    }

    suspend fun updateUserProfile(email: String, name: String, bio: String) = withContext(Dispatchers.IO) {
        val updates = mapOf("name" to name, "bio" to bio)
        db.collection("users").document(email).set(updates, SetOptions.merge()).await()
    }

    suspend fun updatePasswordChangeTimestamp(email: String) = withContext(Dispatchers.IO) {
        db.collection("users").document(email).update("lastPasswordChange", FieldValue.serverTimestamp()).await()
    }

    // --- Trips ---
    suspend fun saveTrip(trip: Trip): String = withContext(Dispatchers.IO) {
        val ref = if (trip.tripId.isEmpty()) {
            db.collection("trips").document()
        } else {
            db.collection("trips").document(trip.tripId)
        }
        val finalTrip = trip.copy(tripId = ref.id)
        ref.set(finalTrip).await()
        ref.id
    }

    suspend fun deleteTrip(tripId: String) = withContext(Dispatchers.IO) {
        if (tripId.isNotEmpty()) {
            db.collection("trips").document(tripId).delete().await()
        }
    }

    fun getPublicTripsListener(onUpdate: (List<Trip>) -> Unit) {
        db.collection("trips")
            .whereEqualTo("public", true)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val trips = snapshot?.toObjects(Trip::class.java) ?: emptyList()
                onUpdate(trips)
            }
    }

    fun getUserTripsListener(email: String, onUpdate: (List<Trip>) -> Unit) {
        db.collection("trips")
            .whereEqualTo("userEmail", email)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val trips = snapshot?.toObjects(Trip::class.java) ?: emptyList()
                onUpdate(trips)
            }
    }

    // --- Likes ---
    suspend fun toggleLike(tripId: String, userEmail: String) = withContext(Dispatchers.IO) {
        val likeId = "${tripId}_${userEmail}"
        val likeRef = db.collection("likes").document(likeId)
        val tripRef = db.collection("trips").document(tripId)

        // THE FIX: Exact math, no more FieldValue.increment bugs!
        db.runTransaction { transaction ->
            val likeSnapshot = transaction.get(likeRef)
            val tripSnapshot = transaction.get(tripRef)

            var currentCount = 0L
            if (tripSnapshot.exists() && tripSnapshot.contains("likeCount")) {
                currentCount = tripSnapshot.getLong("likeCount") ?: 0L
            }

            if (likeSnapshot.exists()) {
                // UNLIKE: Delete like, subtract 1, lock at 0 minimum
                transaction.delete(likeRef)
                currentCount -= 1
                if (currentCount < 0) currentCount = 0
                transaction.update(tripRef, "likeCount", currentCount)
            } else {
                // LIKE: Add like, add 1
                val like = mapOf("likeId" to likeId, "userEmail" to userEmail, "tripId" to tripId)
                transaction.set(likeRef, like)
                currentCount += 1
                transaction.update(tripRef, "likeCount", currentCount)
            }
        }.await()
    }

    suspend fun isTripLikedByUser(tripId: String, userEmail: String): Boolean = withContext(Dispatchers.IO) {
        val likeId = "${tripId}_${userEmail}"
        val doc = db.collection("likes").document(likeId).get().await()
        doc.exists()
    }

    fun getUserLikesListener(userEmail: String, onUpdate: (Set<String>) -> Unit) {
        db.collection("likes")
            .whereEqualTo("userEmail", userEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val likedTripIds = snapshot?.documents?.mapNotNull { it.getString("tripId") }?.toSet() ?: emptySet()
                onUpdate(likedTripIds)
            }
    }

    // --- Comments ---
    suspend fun addComment(tripId: String, userEmail: String, userName: String, text: String) = withContext(Dispatchers.IO) {
        val ref = db.collection("comments").document()
        val comment = Comment(
            commentId = ref.id,
            text = text,
            userEmail = userEmail,
            userName = userName,
            tripId = tripId,
            date = System.currentTimeMillis().toString()
        )
        ref.set(comment).await()
        db.collection("trips").document(tripId)
            .update("commentCount", FieldValue.increment(1)).await()
    }

    fun getCommentsListener(tripId: String, onUpdate: (List<Comment>) -> Unit) {
        db.collection("comments")
            .whereEqualTo("tripId", tripId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                val sortedComments = comments.sortedBy { it.date.toLongOrNull() ?: 0L }
                onUpdate(sortedComments)
            }
    }
}