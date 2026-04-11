package com.example.belajr

import android.app.Activity
import android.content.Intent
import android.widget.ImageView
import androidx.core.content.ContextCompat

object NavigationUtils {

    fun setupBottomNavigation(activity: Activity, activeId: Int) {
        val navDiscovery = activity.findViewById<ImageView>(R.id.nav_discovery)
        val navChat = activity.findViewById<ImageView>(R.id.nav_chat)
        val navFriends = activity.findViewById<ImageView>(R.id.nav_friends)
        val navNotifications = activity.findViewById<ImageView>(R.id.nav_notifications)
        val navProfile = activity.findViewById<ImageView>(R.id.nav_profile)

        // Set active color
        val activeColor = ContextCompat.getColor(activity, R.color.primary)
        val inactiveColor = ContextCompat.getColor(activity, R.color.text_secondary)

        navDiscovery.setColorFilter(if (activeId == R.id.nav_discovery) activeColor else inactiveColor)
        navChat.setColorFilter(if (activeId == R.id.nav_chat) activeColor else inactiveColor)
        navFriends.setColorFilter(if (activeId == R.id.nav_friends) activeColor else inactiveColor)
        navNotifications.setColorFilter(if (activeId == R.id.nav_notifications) activeColor else inactiveColor)
        navProfile.setColorFilter(if (activeId == R.id.nav_profile) activeColor else inactiveColor)

        // Click Listeners
        navDiscovery.setOnClickListener {
            if (activeId != R.id.nav_discovery) {
                activity.startActivity(Intent(activity, HomePage::class.java))
                activity.finish()
            }
        }

        navChat.setOnClickListener {
            if (activeId != R.id.nav_chat) {
                activity.startActivity(Intent(activity, ChatActivity::class.java))
                activity.finish()
            }
        }

        navFriends.setOnClickListener {
            if (activeId != R.id.nav_friends) {
                // Since FriendListActivity is missing, we use HomePage as fallback
                activity.startActivity(Intent(activity, HomePage::class.java))
                activity.finish()
            }
        }

        navNotifications.setOnClickListener {
            if (activeId != R.id.nav_notifications) {
                activity.startActivity(Intent(activity, FriendRequestActivity::class.java))
                activity.finish()
            }
        }

        navProfile.setOnClickListener {
            if (activeId != R.id.nav_profile) {
                activity.startActivity(Intent(activity, ProfileActivity::class.java))
                activity.finish()
            }
        }
    }
}