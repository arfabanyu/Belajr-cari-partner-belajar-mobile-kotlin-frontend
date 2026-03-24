package com.example.belajr.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)

@Serializable
data class ProfileUpdate(
    val username: String? = null,
    val interests: List<String>? = null,
    @SerialName("learning_status")
    val learningStatus: String? = null
)