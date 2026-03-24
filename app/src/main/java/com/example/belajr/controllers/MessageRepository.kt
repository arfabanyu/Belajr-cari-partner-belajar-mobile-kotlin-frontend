package com.example.belajr.controllers

import ChatRoom
import Message
import PartnerResult
import com.example.belajr.SupabaseClient
import com.example.belajr.models.Friendship
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.RealtimeChannel

class MessageRepository {

    private val currentUserId get() =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

    // Ambil riwayat chat dengan satu user
    suspend fun getMessages(otherUserId: String): Result<List<Message>> =
        runCatching {
            SupabaseClient.client.postgrest["messages"]
                .select {
                    filter {
                        or {
                            and {
                                eq("sender_id", currentUserId)
                                eq("receiver_id", otherUserId)
                            }
                            and {
                                eq("sender_id", otherUserId)
                                eq("receiver_id", currentUserId)
                            }
                        }
                    }
                    order("sent_at", Order.ASCENDING)
                }
                .decodeList<Message>()
        }

    // Kirim pesan teks
    suspend fun sendMessage(
        receiverId: String,
        content: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["messages"].insert(
            Message(
                senderId = currentUserId,
                receiverId = receiverId,
                content = content
            )
        )
    }

    // Kirim pesan dengan attachment
    suspend fun sendMessageWithAttachment(
        receiverId: String,
        content: String? = null,
        attachmentUrl: String
    ): Result<Unit> = runCatching {
        SupabaseClient.client.postgrest["messages"].insert(
            Message(
                senderId = currentUserId,
                receiverId = receiverId,
                content = content,
                attachmentUrl = attachmentUrl
            )
        )
    }

    // Realtime: listen pesan masuk dari user tertentu
    fun getChannel(otherUserId: String): RealtimeChannel {
        return SupabaseClient.client.channel("messages-$otherUserId")
    }

    suspend fun subscribeChannel(channel: RealtimeChannel) {
        SupabaseClient.client.realtime.connect()
        channel.subscribe()
    }

    suspend fun unsubscribeChannel(channel: RealtimeChannel) {
        channel.unsubscribe()
        SupabaseClient.client.realtime.removeChannel(channel)
    }

    // Unsubscribe channel saat keluar dari chat
    suspend fun unlistenMessages(otherUserId: String) {
        val channel = SupabaseClient.client.channel("messages-$otherUserId")
        channel.unsubscribe()
        SupabaseClient.client.realtime.removeChannel(channel)
    }

    // Ambil daftar chat room (semua teman + last message)
    suspend fun getChatRooms(friends: List<Friendship>): Result<List<ChatRoom>> =
        runCatching {
            friends.map { friendship ->
                // Tentukan ID teman dari friendship
                val friendId = if (friendship.userOneId == currentUserId)
                    friendship.userTwoId else friendship.userOneId

                // Ambil profil teman
                val friendProfile = SupabaseClient.client.postgrest["profiles"]
                    .select { filter { eq("id", friendId) } }
                    .decodeSingle<PartnerResult>()

                // Ambil pesan terakhir
                val lastMessage = SupabaseClient.client.postgrest["messages"]
                    .select {
                        filter {
                            or {
                                and {
                                    eq("sender_id", currentUserId)
                                    eq("receiver_id", friendId)
                                }
                                and {
                                    eq("sender_id", friendId)
                                    eq("receiver_id", currentUserId)
                                }
                            }
                        }
                        order("sent_at", Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<Message>()
                    .firstOrNull()

                ChatRoom(friendProfile, lastMessage)
            }
        }
}