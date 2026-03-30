package com.example.belajr.controllers

import io.github.jan.supabase.auth.auth
import android.content.Context
import android.net.Uri
import com.example.belajr.SupabaseClient
import io.github.jan.supabase.storage.storage
import java.io.InputStream

class AttachmentRepository(private val context: Context) {

    private val currentUserId get() =
        SupabaseClient.client.auth.currentUserOrNull()?.id
            ?: error("Belum login")

    suspend fun uploadFile(uri: Uri): Result<String> = runCatching {

        val inputStream: InputStream = context.contentResolver
            .openInputStream(uri)
            ?: error("Gagal buka file")

        val bytes: ByteArray = inputStream.use { it.readBytes() }

        val fileName = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "file_${System.currentTimeMillis()}"

        val mimeType = context.contentResolver.getType(uri)
            ?: "application/octet-stream"

        val storagePath = "$currentUserId/${System.currentTimeMillis()}_$fileName"

        SupabaseClient.client.storage["attachments"].upload(
            path = storagePath,
            data = bytes,
            options = {
                contentType = io.ktor.http.ContentType.parse(mimeType)
                upsert = false
            }
        )

        SupabaseClient.client.storage["attachments"]
            .publicUrl(storagePath)
    }

    suspend fun deleteFile(attachmentUrl: String): Result<Unit> = runCatching {
        val path = attachmentUrl
            .substringAfter("/storage/v1/object/public/attachments/")

        SupabaseClient.client.storage["attachments"].delete(path)
    }
}