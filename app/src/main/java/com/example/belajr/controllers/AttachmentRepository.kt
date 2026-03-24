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

    // Upload file ke Supabase Storage
    // dan return public URL-nya
    suspend fun uploadFile(uri: Uri): Result<String> = runCatching {

        // 1. Buka file dari URI
        val inputStream: InputStream = context.contentResolver
            .openInputStream(uri)
            ?: error("Gagal buka file")

        val bytes: ByteArray = inputStream.use { it.readBytes() }

        // 2. Ambil nama file dari URI
        val fileName = context.contentResolver
            .query(uri, null, null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(
                    android.provider.OpenableColumns.DISPLAY_NAME
                )
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "file_${System.currentTimeMillis()}"

        // 3. Tentukan MIME type
        val mimeType = context.contentResolver.getType(uri)
            ?: "application/octet-stream"

        // 4. Path di storage: {userId}/{timestamp}_{fileName}
        val storagePath = "$currentUserId/${System.currentTimeMillis()}_$fileName"

        // 5. Upload ke bucket attachments
        SupabaseClient.client.storage["attachments"].upload(
            path = storagePath,
            data = bytes,
            options = {
                contentType = io.ktor.http.ContentType.parse(mimeType)
                upsert = false
            }
        )

        // 6. Return public URL
        SupabaseClient.client.storage["attachments"]
            .publicUrl(storagePath)
    }

    // Hapus file dari storage
    suspend fun deleteFile(attachmentUrl: String): Result<Unit> = runCatching {
        // Extract path dari URL
        val path = attachmentUrl
            .substringAfter("/storage/v1/object/public/attachments/")

        SupabaseClient.client.storage["attachments"].delete(path)
    }
}