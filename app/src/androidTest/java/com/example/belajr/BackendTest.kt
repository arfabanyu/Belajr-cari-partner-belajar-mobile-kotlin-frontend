package com.example.belajr

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.belajr.controllers.AuthRepository
import com.example.belajr.controllers.FriendRepository
import com.example.belajr.controllers.MatchRepository
import com.example.belajr.controllers.MessageRepository
import com.example.belajr.controllers.NotificationRepository
import com.example.belajr.models.ProfileUpdate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class BackendTest {

    private val TAG = "BELAJR_TEST"

    private val testEmail1 = "testuser1@gmail.com"
    private val testEmail2 = "testuser2@gmail.com"
    private val testEmail3 = "testuser3@gmail.com"
    private val testPassword = "password123"
    private val testUsername1 = "testuser1"
    private val testUsername2 = "testuser2"
    private val testUsername3 = "testuser3"

    // ============================================
    // AUTH TESTS
    // ============================================

    @Test
    fun test01_register_user1() {
        runBlocking {
            val repo = AuthRepository()
            val result = repo.register(testEmail1, testPassword, testUsername1)
            Log.d(TAG, "Register user1: $result")
            Log.d(TAG, "Register user1 selesai")
        }
    }

    @Test
    fun test02_register_user2() {
        runBlocking {
            val repo = AuthRepository()
            val result = repo.register(testEmail2, testPassword, testUsername2)
            Log.d(TAG, "Register user2: $result")
            Log.d(TAG, "Register user2 selesai")
        }
    }

    @Test
    fun test03_login_success() {
        runBlocking {
            val repo = AuthRepository()
            val result = repo.login(testEmail1, testPassword)
            Log.d(TAG, "Login result: $result")
            assertTrue("Login harus berhasil", result.isSuccess)
        }
    }

    @Test
    fun test04_login_wrong_password() {
        runBlocking {
            val repo = AuthRepository()
            val result = repo.login(testEmail1, "passwordsalah")
            Log.d(TAG, "Login password salah: $result")
            assertTrue("Login password salah harus gagal", result.isFailure)
        }
    }

    @Test
    fun test05_get_current_profile() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val result = authRepo.getCurrentProfile()
            Log.d(TAG, "Get profile: $result")
            assertTrue("Get profile harus berhasil", result.isSuccess)

            val profile = result.getOrNull()
            Log.d(TAG, "Profile username: ${profile?.username}")
            Log.d(TAG, "Profile email: ${profile?.email}")
        }
    }

    @Test
    fun test06_update_profile() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val result = authRepo.updateProfile(
                ProfileUpdate(
                    username = testUsername1,
                    interests = listOf("Matematika", "Fisika"),
                    learningStatus = "active"
                )
            )
            Log.d(TAG, "Update profile: $result")
            assertTrue("Update profile harus berhasil", result.isSuccess)
        }
    }

    // ============================================
    // NOTIFICATION TESTS
    // ============================================

    @Test
    fun test07_register_fcm_token() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val result = NotificationRepository().registerFcmToken()
            Log.d(TAG, "Register FCM token: $result")
            Log.d(TAG, "FCM token test selesai")
        }
    }

    @Test
    fun test20_clear_fcm_token() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val result = NotificationRepository().clearFcmToken()
            Log.d(TAG, "Clear FCM token: $result")
            assertTrue("Clear FCM token harus berhasil", result.isSuccess)
            authRepo.logout()
        }
    }

    // ============================================
    // MATCH TESTS
    // ============================================

    @Test
    fun test08_search_partner() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            authRepo.logout()
            authRepo.login(testEmail2, testPassword)
            authRepo.updateProfile(ProfileUpdate(interests = listOf("Matematika", "Fisika")))
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val matchRepo = MatchRepository()
            val result = matchRepo.searchPartners("Matematika")
            Log.d(TAG, "Search partner: $result")
            assertTrue("Search harus berhasil", result.isSuccess)

            val partners = result.getOrNull()
            Log.d(TAG, "Jumlah partner ditemukan: ${partners?.size}")
            partners?.forEach {
                Log.d(TAG, "Partner: ${it.profile.username}, status: ${it.relationStatus}")
            }
            authRepo.logout()
        }
    }

    @Test
    fun test23_search_empty_keyword() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val matchRepo = MatchRepository()
            val result = matchRepo.searchPartners("")
            Log.d(TAG, "Search keyword kosong: $result")
            Log.d(TAG, "Search empty selesai: ${result.getOrNull()?.size} hasil")
            authRepo.logout()
        }
    }

    @Test
    fun test24_search_no_result() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val matchRepo = MatchRepository()
            val result = matchRepo.searchPartners("xyzabcnotexist123")
            Log.d(TAG, "Search tidak ada hasil: $result")
            assertTrue("Search harus berhasil meski tidak ada hasil", result.isSuccess)

            val partners = result.getOrNull()
            Log.d(TAG, "Jumlah hasil: ${partners?.size}")
            assertTrue("Hasilnya harus kosong", partners?.isEmpty() == true)
            authRepo.logout()
        }
    }

    // ============================================
    // FRIEND REQUEST TESTS
    // ============================================

    @Test
    fun test09_send_friend_request() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)
            val user2Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            Log.d(TAG, "User2 ID: $user2Id")
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val friendRepo = FriendRepository()
            val result = friendRepo.sendRequest(user2Id)
            Log.d(TAG, "Send friend request: $result")
            assertTrue("Kirim request harus berhasil", result.isSuccess)
            authRepo.logout()
        }
    }

    @Test
    fun test10_get_incoming_requests() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)

            val friendRepo = FriendRepository()
            val result = friendRepo.getIncomingRequests()
            Log.d(TAG, "Incoming requests: $result")
            assertTrue("Get incoming requests harus berhasil", result.isSuccess)

            val requests = result.getOrNull()
            Log.d(TAG, "Jumlah request masuk: ${requests?.size}")
            requests?.forEach {
                Log.d(TAG, "Request dari: ${it.senderId}, status: ${it.status}")
            }
            authRepo.logout()
        }
    }

    @Test
    fun test11_accept_friend_request() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)

            val friendRepo = FriendRepository()
            val requests = friendRepo.getIncomingRequests().getOrNull()
            val firstRequest = requests?.firstOrNull()

            if (firstRequest == null) {
                Log.d(TAG, "Tidak ada request masuk, skip test ini")
                return@runBlocking
            }

            val result = friendRepo.acceptRequest(
                requestId = firstRequest.id!!,
                senderId = firstRequest.senderId
            )
            Log.d(TAG, "Accept request: $result")
            assertTrue("Accept request harus berhasil", result.isSuccess)
            authRepo.logout()
        }
    }

    @Test
    fun test12_get_friends() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val friendRepo = FriendRepository()
            val result = friendRepo.getFriends()
            Log.d(TAG, "Get friends: $result")
            assertTrue("Get friends harus berhasil", result.isSuccess)

            val friends = result.getOrNull()
            Log.d(TAG, "Jumlah teman: ${friends?.size}")
            authRepo.logout()
        }
    }

    @Test
    fun test13_is_friend() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)
            val user2Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val friendRepo = FriendRepository()
            val isFriend = friendRepo.isFriend(user2Id)
            Log.d(TAG, "Is friend dengan user2: $isFriend")
            assertTrue("Seharusnya sudah berteman", isFriend)
            authRepo.logout()
        }
    }

    @Test
    fun test18_get_outgoing_requests() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val friendRepo = FriendRepository()
            val result = friendRepo.getOutgoingRequests()
            Log.d(TAG, "Outgoing requests: $result")
            assertTrue("Get outgoing requests harus berhasil", result.isSuccess)

            val requests = result.getOrNull()
            Log.d(TAG, "Jumlah request keluar: ${requests?.size}")
            requests?.forEach {
                Log.d(TAG, "Request ke: ${it.receiverId}, status: ${it.status}")
            }
            authRepo.logout()
        }
    }

    @Test
    fun test19_reject_friend_request() {
        runBlocking {
            val authRepo = AuthRepository()

            authRepo.register(testEmail3, testPassword, testUsername3)
            authRepo.logout()

            authRepo.login(testEmail3, testPassword)
            authRepo.updateProfile(ProfileUpdate(interests = listOf("Biologi")))
            val user3Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val friendRepo = FriendRepository()
            friendRepo.sendRequest(user3Id)
            authRepo.logout()

            authRepo.login(testEmail3, testPassword)
            val incomingRequests = friendRepo.getIncomingRequests().getOrNull()
            val requestToReject = incomingRequests?.firstOrNull()

            if (requestToReject == null) {
                Log.d(TAG, "Tidak ada request masuk untuk direject, skip")
                return@runBlocking
            }

            val result = friendRepo.rejectRequest(requestToReject.id!!)
            Log.d(TAG, "Reject request: $result")
            assertTrue("Reject request harus berhasil", result.isSuccess)

            val updatedRequests = friendRepo.getIncomingRequests().getOrNull()
            val stillPending = updatedRequests?.any { it.id == requestToReject.id }
            Log.d(TAG, "Request masih pending: $stillPending")
            authRepo.logout()
        }
    }

    // ============================================
    // MESSAGE TESTS
    // ============================================

    @Test
    fun test14_send_message() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)
            val user2Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val messageRepo = MessageRepository()
            val result = messageRepo.sendMessage(
                receiverId = user2Id,
                content = "Halo, ayo belajar bareng!"
            )
            Log.d(TAG, "Send message: $result")
            assertTrue("Kirim pesan harus berhasil", result.isSuccess)
            authRepo.logout()
        }
    }

    @Test
    fun test15_get_messages() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)
            val user2Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val messageRepo = MessageRepository()
            val result = messageRepo.getMessages(user2Id)
            Log.d(TAG, "Get messages: $result")
            assertTrue("Get messages harus berhasil", result.isSuccess)

            val messages = result.getOrNull()
            Log.d(TAG, "Jumlah pesan: ${messages?.size}")
            messages?.forEach {
                Log.d(TAG, "Pesan: ${it.content}, dari: ${it.senderId}")
            }
            authRepo.logout()
        }
    }

    @Test
    fun test16_get_chat_rooms() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val friendRepo = FriendRepository()
            val friends = friendRepo.getFriends().getOrNull() ?: emptyList()

            val messageRepo = MessageRepository()
            val result = messageRepo.getChatRooms(friends)
            Log.d(TAG, "Get chat rooms: $result")
            assertTrue("Get chat rooms harus berhasil", result.isSuccess)

            val rooms = result.getOrNull()
            Log.d(TAG, "Jumlah chat room: ${rooms?.size}")
            rooms?.forEach {
                Log.d(TAG, "Room dengan: ${it.friend.username}, last message: ${it.lastMessage?.content}")
            }
            authRepo.logout()
        }
    }

    @Test
    fun test21_send_message_with_attachment() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail2, testPassword)
            val user2Id = authRepo.getCurrentProfile().getOrNull()?.id ?: return@runBlocking
            authRepo.logout()

            authRepo.login(testEmail1, testPassword)
            val messageRepo = MessageRepository()
            val result = messageRepo.sendMessageWithAttachment(
                receiverId = user2Id,
                content = "Ini catatan matematika gw",
                attachmentUrl = "https://example.com/test-attachment.pdf"
            )
            Log.d(TAG, "Send message with attachment: $result")
            assertTrue("Kirim pesan dengan attachment harus berhasil", result.isSuccess)

            val messages = messageRepo.getMessages(user2Id).getOrNull()
            val messageWithAttachment = messages?.firstOrNull { it.attachmentUrl != null }
            Log.d(TAG, "Pesan dengan attachment: ${messageWithAttachment?.content}")
            Log.d(TAG, "Attachment URL: ${messageWithAttachment?.attachmentUrl}")
            authRepo.logout()
        }
    }

    // ============================================
    // LOGOUT TESTS
    // ============================================

    @Test
    fun test17_logout() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.login(testEmail1, testPassword)

            val result = authRepo.logout()
            Log.d(TAG, "Logout: $result")
            assertTrue("Logout harus berhasil", result.isSuccess)

            val isLoggedIn = authRepo.isLoggedIn()
            Log.d(TAG, "Is logged in setelah logout: $isLoggedIn")
            assertFalse("Seharusnya sudah logout", isLoggedIn)
        }
    }

    @Test
    fun test22_is_logged_in_after_logout() {
        runBlocking {
            val authRepo = AuthRepository()
            authRepo.logout()

            val isLoggedIn = authRepo.isLoggedIn()
            Log.d(TAG, "Is logged in setelah logout: $isLoggedIn")
            assertFalse("Seharusnya tidak logged in", isLoggedIn)
        }
    }
}