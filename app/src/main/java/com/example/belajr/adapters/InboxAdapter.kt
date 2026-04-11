package com.example.belajr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.ChatRoom
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class InboxAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    class InboxViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFriendName: TextView = view.findViewById(R.id.tvFriendName)
        val tvLastMessage: TextView = view.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = view.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room, parent, false)
        return InboxViewHolder(view)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        val room = chatRooms[position]
        holder.tvFriendName.text = room.friend.username
        holder.tvLastMessage.text = room.lastMessage?.content ?: "No messages yet"
        
        // Memformat waktu dari database (UTC) ke waktu lokal (WIB) dalam format 24 jam
        val sentAt = room.lastMessage?.sentAt
        if (sentAt != null) {
            try {
                // Format ISO 8601 dari Supabase
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(sentAt)
                
                // Output format 24 jam (HH:mm) tanpa tulisan tambahan
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                outputFormat.timeZone = TimeZone.getDefault() // Mengikuti settingan HP (WIB)
                
                holder.tvTime.text = date?.let { outputFormat.format(it) } ?: ""
            } catch (e: Exception) {
                // Fallback jika terjadi error parsing
                val fullTime = sentAt.substringAfter("T").substringBefore(".")
                holder.tvTime.text = if (fullTime.length >= 5) fullTime.substring(0, 5) else fullTime
            }
        } else {
            holder.tvTime.text = ""
        }

        holder.itemView.setOnClickListener { onClick(room) }
    }

    override fun getItemCount() = chatRooms.size

    fun updateData(newRooms: List<ChatRoom>) {
        chatRooms = newRooms
        notifyDataSetChanged()
    }
}