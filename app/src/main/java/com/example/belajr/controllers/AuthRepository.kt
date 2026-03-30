package com.example.belajr.controllers

import com.example.belajr.models.ProfileUpdate
import com.example.belajr.SupabaseClient
import com.example.belajr.models.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class AuthRepository {

    suspend fun register(
        email: String,
        password: String,
        username: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Register gagal")

        SupabaseClient.client.postgrest["profiles"]
            .update({ set("username", username) }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout(): Result<Unit> = runCatching {
        SupabaseClient.client.auth.signOut()
    }

    suspend fun getCurrentProfile(): Result<Profile> = runCatching {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

        SupabaseClient.client.postgrest["profiles"]
            .select { filter { eq("id", userId) } }
            .decodeSingle<Profile>()
    }

    suspend fun updateProfile(
        data: ProfileUpdate
    ): Result<Unit> = runCatching {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

        SupabaseClient.client.postgrest["profiles"]
            .update(data) { filter { eq("id", userId) } }
    }

    fun isLoggedIn(): Boolean {
        return SupabaseClient.client.auth.currentUserOrNull() != null
    }
}