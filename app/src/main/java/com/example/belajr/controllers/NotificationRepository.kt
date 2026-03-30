package com.example.belajr.controllers

import com.example.belajr.SupabaseClient
import com.google.firebase.messaging.FirebaseMessaging
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.tasks.await

class NotificationRepository {

    private val currentUserId get() =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

    suspend fun registerFcmToken(): Result<Unit> = runCatching {
        val token = FirebaseMessaging.getInstance().token.await()

        SupabaseClient.client.postgrest["profiles"]
            .update({ set("fcm_token", token) }) {
                filter { eq("id", currentUserId) }
            }
    }

    suspend fun clearFcmToken(): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["profiles"]
            .update({ set("fcm_token", null as String?) }) {
                filter { eq("id", currentUserId) }
            }
    }
}