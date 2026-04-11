package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.belajr.adapters.PartnerAdapter
import com.example.belajr.adapters.Subject
import com.example.belajr.adapters.SubjectAdapter
import com.example.belajr.models.RelationStatus
import com.example.belajr.views.AuthViewModel
import com.example.belajr.views.MatchViewModel
import kotlinx.coroutines.launch

class HomePage : AppCompatActivity() {

    private lateinit var matchViewModel: MatchViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var partnerAdapter: PartnerAdapter
    private lateinit var subjectAdapter: SubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home_page)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        matchViewModel = ViewModelProvider(this)[MatchViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]

        setupSubjectRecyclerView()
        setupPartnerRecyclerView()
        
        NavigationUtils.setupBottomNavigation(this, R.id.nav_discovery)
        
        observeData()

        // Load user profile to get interests for initial recommendations
        authViewModel.loadProfile()
    }

    private fun setupSubjectRecyclerView() {
        val rvSubjects = findViewById<RecyclerView>(R.id.rvSubjects)
        // Default subjects, can be expanded or fetched from DB if there's a table for it
        val subjects = listOf(
            Subject("All", R.drawable.sharp_align_flex_center_24),
            Subject("Math", R.drawable.ic_launcher_foreground),
            Subject("Coding", R.drawable.ic_launcher_foreground),
            Subject("English", R.drawable.ic_launcher_foreground),
            Subject("Network", R.drawable.ic_launcher_foreground),
            Subject("History", R.drawable.ic_launcher_foreground)
        )

        subjectAdapter = SubjectAdapter(subjects) { subject ->
            if (subject.name == "All") {
                matchViewModel.searchPartners("")
            } else {
                matchViewModel.searchPartners(subject.name)
            }
        }
        rvSubjects.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvSubjects.adapter = subjectAdapter
    }

    private fun setupPartnerRecyclerView() {
        val rvPartners = findViewById<RecyclerView>(R.id.rvPartners)
        partnerAdapter = PartnerAdapter(emptyList()) { partner ->
            when (partner.relationStatus) {
                RelationStatus.NONE -> {
                    matchViewModel.sendRequest(partner.profile.id, "")
                }
                RelationStatus.FRIEND -> {
                    val intent = Intent(this, ChatDetailActivity::class.java).apply {
                        putExtra("RECEIVER_ID", partner.profile.id)
                        putExtra("RECEIVER_NAME", partner.profile.username)
                    }
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(this, OtherProfileActivity::class.java).apply {
                        putExtra("USER_ID", partner.profile.id)
                        putExtra("USERNAME", partner.profile.username)
                        putExtra("RELATION_STATUS", partner.relationStatus.name)
                        putExtra("INTERESTS", partner.profile.interests?.joinToString(", "))
                        putExtra("BIO", partner.profile.learningStatus)
                    }
                    startActivity(intent)
                }
            }
        }
        rvPartners.layoutManager = LinearLayoutManager(this)
        rvPartners.adapter = partnerAdapter
    }

    private fun observeData() {
        // Observe partners from MatchViewModel
        matchViewModel.partners.observe(this) { partners ->
            partnerAdapter.updateData(partners)
        }

        // Observe current user profile to personalize HomePage
        lifecycleScope.launch {
            authViewModel.profile.collect { profile ->
                if (profile != null) {
                    // Search partners based on user's first interest as initial recommendation
                    val primaryInterest = profile.interests?.firstOrNull() ?: ""
                    matchViewModel.searchPartners(primaryInterest)
                }
            }
        }
    }
}