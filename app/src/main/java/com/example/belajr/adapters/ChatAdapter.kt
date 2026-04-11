package com.example.belajr.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.R
import com.example.belajr.models.Message
import io.github.jan.supabase.auth.auth
import com.example.belajr.SupabaseClient
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import com.bumptech.glide.Glide
import com.example.belajr.FullImageActivity

class ChatAdapter(
    private var messages: List<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_SENT = 1
    private val VIEW_TYPE_RECEIVED = 2
    
    private val currentUserId = SupabaseClient.client.auth.currentUserOrNull()?.id

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_right, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_left, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateMessages(newMessages: List<Message>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    class SentMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivAttachment: ImageView = view.findViewById(R.id.ivAttachment)

        fun bind(message: Message) {
            tvMessage.text = message.content
            tvTime.text = formatTime(message.sentAt)
            
            if (!message.attachmentUrl.isNullOrEmpty()) {
                ivAttachment.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.attachmentUrl)
                    .into(ivAttachment)
                
                ivAttachment.setOnClickListener {
                    val intent = Intent(itemView.context, FullImageActivity::class.java).apply {
                        putExtra("IMAGE_URL", message.attachmentUrl)
                    }
                    itemView.context.startActivity(intent)
                }
            } else {
                ivAttachment.visibility = View.GONE
            }
        }
    }

    class ReceivedMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvMessage: TextView = view.findViewById(R.id.tvMessage)
        private val tvTime: TextView = view.findViewById(R.id.tvTime)
        private val ivAttachment: ImageView = view.findViewById(R.id.ivAttachment)

        fun bind(message: Message) {
            tvMessage.text = message.content
            tvTime.text = formatTime(message.sentAt)
            
            if (!message.attachmentUrl.isNullOrEmpty()) {
                ivAttachment.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(message.attachmentUrl)
                    .into(ivAttachment)

                ivAttachment.setOnClickListener {
                    val intent = Intent(itemView.context, FullImageActivity::class.java).apply {
                        putExtra("IMAGE_URL", message.attachmentUrl)
                    }
                    itemView.context.startActivity(intent)
                }
            } else {
                ivAttachment.visibility = View.GONE
            }
        }
    }
}

private fun formatTime(sentAt: String?): String {
    if (sentAt == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(sentAt)
        
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        
        date?.let { outputFormat.format(it) } ?: ""
    } catch (e: Exception) {
        val fullTime = sentAt.substringAfter("T").substringBefore(".")
        if (fullTime.length >= 5) fullTime.substring(0, 5) else fullTime
    }
}
