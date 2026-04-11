package com.example.belajr

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.belajr.adapters.ChatAdapter
import com.example.belajr.views.MessageViewModel
import kotlinx.coroutines.launch

class ChatDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: MessageViewModel
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageView
    private lateinit var btnAttach: ImageView
    private lateinit var tvOnlineStatus: TextView
    
    // Preview Components
    private lateinit var cvPreviewContainer: CardView
    private lateinit var ivPreview: ImageView
    private lateinit var btnCancelPreview: ImageView
    private var selectedImageUri: Uri? = null
    
    private var receiverId: String? = null
    private var receiverName: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            showPreview(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_chat_detail)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left, 
                systemBars.top, 
                systemBars.right, 
                if (ime.bottom > 0) ime.bottom else systemBars.bottom
            )
            insets
        }

        receiverId = intent.getStringExtra("RECEIVER_ID")
        receiverName = intent.getStringExtra("RECEIVER_NAME")

        if (receiverId == null) {
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        
        setupViews()
        setupRecyclerView()
        observeData()

        viewModel.loadChatRooms()
        viewModel.openChat(receiverId!!)
    }

    private fun setupViews() {
        findViewById<TextView>(R.id.tvUserName).text = receiverName ?: "Chat"
        tvOnlineStatus = findViewById(R.id.tvOnlineStatus)
        findViewById<ImageView>(R.id.ivBack).setOnClickListener { finish() }
        
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        
        // Preview Setup
        cvPreviewContainer = findViewById(R.id.cvPreviewContainer)
        ivPreview = findViewById(R.id.ivPreview)
        btnCancelPreview = findViewById(R.id.btnCancelPreview)

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            val hasImage = selectedImageUri != null
            val hasText = content.isNotEmpty()

            if (hasImage) {
                viewModel.sendMessageWithImage(this, receiverId!!, if (hasText) content else null, selectedImageUri!!)
                clearPreview()
                etMessage.text.clear()
            } else if (hasText) {
                viewModel.sendMessage(receiverId!!, content)
                etMessage.text.clear()
            }
        }

        btnAttach.setOnClickListener {
            pickImage.launch("image/*")
        }

        btnCancelPreview.setOnClickListener {
            clearPreview()
        }
    }

    private fun showPreview(uri: Uri) {
        cvPreviewContainer.visibility = View.VISIBLE
        Glide.with(this).load(uri).into(ivPreview)
    }

    private fun clearPreview() {
        selectedImageUri = null
        cvPreviewContainer.visibility = View.GONE
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(emptyList())
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        rvMessages.adapter = chatAdapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    chatAdapter.updateMessages(messages)
                    if (messages.isNotEmpty()) {
                        rvMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activeFriend.collect { friend ->
                    if (friend != null && friend.id == receiverId) {
                        tvOnlineStatus.text = if (friend.isOnline) "Online" else "Offline"
                        tvOnlineStatus.setTextColor(
                            if (friend.isOnline) android.graphics.Color.GREEN else android.graphics.Color.LTGRAY
                        )
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.error.collect { error ->
                if (error != null) {
                    Toast.makeText(this@ChatDetailActivity, error, Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        lifecycleScope.launch {
            viewModel.isLoading.collect { loading ->
                btnAttach.isEnabled = !loading
                btnSend.isEnabled = !loading
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.closeChat()
    }
}
