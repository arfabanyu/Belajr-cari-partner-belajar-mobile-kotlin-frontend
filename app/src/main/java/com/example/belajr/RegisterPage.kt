package com.example.belajr

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ProfileInsert(
    val id: String,
    val email: String,
    val username: String
)

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        val usernameEditText = findViewById<EditText>(R.id.username)
        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPassword)
        val registerButton = findViewById<Button>(R.id.registerButton)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            when {
                username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                    Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                }
                else -> registerUser(username, email, password)
            }
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val result = SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }

                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                    ?: throw Exception("Gagal mendapatkan user ID")

                SupabaseClient.client.postgrest["profiles"]
                    .update({ set("username", username) }) {
                        filter { eq("id", userId) }
                    }

                Toast.makeText(
                    this@RegisterPage,
                    "Registrasi berhasil! Silakan login.",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(Intent(this@RegisterPage, LoginPage::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(
                    this@RegisterPage,
                    "Registrasi gagal: ${e.message ?: e.toString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}