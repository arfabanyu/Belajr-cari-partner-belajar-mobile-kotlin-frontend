package com.example.belajr.models
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class PartnerResult(
    val id: String,
    val username: String,
    val interests: List<String>? = null,
    @SerialName("learning_status")
    val learningStatus: String? = null,
    @SerialName("is_online")
    val isOnline: Boolean = false
)

enum class RelationStatus {
    NONE,
    PENDING_OUT,
    PENDING_IN,
    FRIEND
}

data class PartnerWithStatus(
    val profile: PartnerResult,
    val relationStatus: RelationStatus,
    val requestId: Long? = null
)