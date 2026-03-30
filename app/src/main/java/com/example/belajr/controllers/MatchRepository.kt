package com.example.belajr.controllers

import PartnerResult
import PartnerWithStatus
import RelationStatus
import com.example.belajr.SupabaseClient
import com.example.belajr.models.FriendRequest
import com.example.belajr.models.Friendship
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.auth.auth

class MatchRepository {

    private val currentUserId get() =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

    suspend fun searchPartners(keyword: String): Result<List<PartnerWithStatus>> =
        runCatching {
            val profiles = SupabaseClient.client.postgrest["profiles"]
                .select {
                    filter {
                        contains("interests", listOf(keyword))
                        neq("id", currentUserId)
                    }
                }
                .decodeList<PartnerResult>()

            val requests = SupabaseClient.client.postgrest["friend_requests"]
                .select {
                    filter {
                        or {
                            eq("sender_id", currentUserId)
                            eq("receiver_id", currentUserId)
                        }
                    }
                }
                .decodeList<FriendRequest>()

            val friendships = SupabaseClient.client.postgrest["friendships"]
                .select {
                    filter {
                        or {
                            eq("user_one_id", currentUserId)
                            eq("user_two_id", currentUserId)
                        }
                    }
                }
                .decodeList<Friendship>()

            profiles.map { profile ->
                val status = determineStatus(
                    profile.id,
                    requests,
                    friendships
                )
                PartnerWithStatus(profile, status)
            }
        }

    private fun determineStatus(
        otherUserId: String,
        requests: List<FriendRequest>,
        friendships: List<Friendship>
    ): RelationStatus {

        val isFriend = friendships.any {
            it.userOneId == otherUserId || it.userTwoId == otherUserId
        }
        if (isFriend) return RelationStatus.FRIEND

        val request = requests.firstOrNull {
            (it.senderId == otherUserId || it.receiverId == otherUserId)
                    && it.status == "pending"
        }

        return when {
            request == null -> RelationStatus.NONE
            request.senderId == currentUserId -> RelationStatus.PENDING_OUT
            else -> RelationStatus.PENDING_IN
        }
    }
}