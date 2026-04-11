package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.belajr.views.AuthViewModel
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Setup Bottom Navigation
        NavigationUtils.setupBottomNavigation(this, R.id.nav_profile)

        setupViews()
        observeProfile()

        authViewModel.loadProfile()
    }

    private fun setupViews() {
        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            authViewModel.logout()
            val intent = Intent(this, LoginPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        // Setup other menu actions if needed
    }

    private fun observeProfile() {
        lifecycleScope.launch {
            authViewModel.profile.collect { profile ->
                if (profile != null) {
                    findViewById<TextView>(R.id.tvName).text = profile.username
                    // You can add more profile fields here (email, bio, etc.)
                }
            }
        }
    }
}