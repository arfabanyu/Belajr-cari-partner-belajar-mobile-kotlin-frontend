package com.example.belajr.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Profile(
    val id: String,
    val email: String,
    val username: String,
    val interests: List<String>? = null,
    @SerialName("learning_status")
    val learningStatus: String? = null
)

@Serializable
data class FriendRequest(
    val id: Long? = null,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("receiver_id")
    val receiverId: String,
    val status: String = "pending",
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Friendship(
    val id: Long? = null,
    @SerialName("user_one_id")
    val userOneId: String,
    @SerialName("user_two_id")
    val userTwoId: String,
    @SerialName("created_at")
    val createdAt: String? = null
)