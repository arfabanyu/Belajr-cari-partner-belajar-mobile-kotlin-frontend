package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.InboxAdapter
import com.example.belajr.adapters.OnlineFriendAdapter
import com.example.belajr.models.ChatRoom
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.MessageViewModel
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageViewModel
    private lateinit var inboxAdapter: InboxAdapter
    private lateinit var onlineAdapter: OnlineFriendAdapter
    
    private lateinit var rvChatList: RecyclerView
    private lateinit var rvOnlineFriends: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var llIndicators: LinearLayout
    
    private var allChatRooms: List<ChatRoom> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        setupViews()
        setupRecyclerViews()
        NavigationUtils.setupBottomNavigation(this, R.id.nav_chat)
        observeData()

        viewModel.loadChatRooms()
    }

    private fun setupViews() {
        rvChatList = findViewById(R.id.rvChatList)
        rvOnlineFriends = findViewById(R.id.rvOnlineFriends)
        etSearch = findViewById(R.id.etSearch)
        llIndicators = findViewById(R.id.llIndicators)

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterChats(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterChats(query: String) {
        val filtered = if (query.isEmpty()) {
            allChatRooms
        } else {
            allChatRooms.filter { 
                it.friend.username.contains(query, ignoreCase = true) 
            }
        }
        inboxAdapter.updateData(filtered)
    }

    private fun setupRecyclerViews() {
        inboxAdapter = InboxAdapter(emptyList()) { room ->
            val intent = Intent(this, ChatDetailActivity::class.java).apply {
                putExtra("RECEIVER_ID", room.friend.id)
                putExtra("RECEIVER_NAME", room.friend.username)
            }
            startActivity(intent)
        }
        rvChatList.layoutManager = LinearLayoutManager(this)
        rvChatList.adapter = inboxAdapter

        // Bagian Teman Online (Carousel)
        onlineAdapter = OnlineFriendAdapter(emptyList()) { friend ->
            // Ketika diklik, masuk ke OtherProfileActivity
            val intent = Intent(this, OtherProfileActivity::class.java).apply {
                putExtra("USER_ID", friend.id)
                putExtra("USERNAME", friend.username)
                putExtra("BIO", friend.learningStatus)
                putExtra("INTERESTS", friend.interests?.joinToString(", "))
                // Karena mereka teman chat, asumsikan statusnya FRIEND
                putExtra("RELATION_STATUS", RelationStatus.FRIEND.name)
            }
            startActivity(intent)
        }
        rvOnlineFriends.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvOnlineFriends.adapter = onlineAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.chatRooms.collect { rooms ->
                    allChatRooms = rooms
                    inboxAdapter.updateData(rooms)
                    
                    val onlineFriends = rooms.map { it.friend }
                    onlineAdapter.updateData(onlineFriends)
                    
                    updateIndicators(onlineFriends.size)
                }
            }
        }
    }

    private fun updateIndicators(count: Int) {
        llIndicators.removeAllViews()
        if (count <= 1) return

        for (i in 0 until count) {
            val dot = View(this).apply {
                val size = (6 * resources.displayMetrics.density).toInt()
                val margin = (2 * resources.displayMetrics.density).toInt()
                val params = LinearLayout.LayoutParams(size, size).apply {
                    setMargins(margin, margin, margin, margin)
                }
                layoutParams = params
                background = if (i == 0) {
                    getDrawable(R.drawable.bg_indicator_active)
                } else {
                    getDrawable(R.drawable.bg_indicator_inactive)
                }
            }
            llIndicators.addView(dot)
        }
    }
}
