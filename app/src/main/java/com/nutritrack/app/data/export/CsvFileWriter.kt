package com.nutritrack.app.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object CsvFileWriter {

    fun write(context: Context, fileName: String, csvContent: String): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeViaMediaStore(context, fileName, csvContent)
        } else {
            writeToLegacyDownloads(fileName, csvContent)
        }

    private fun writeViaMediaStore(context: Context, fileName: String, csvContent: String): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
        return try {
            resolver.openOutputStream(uri)?.use { it.write(csvContent.toByteArray()) }
            uri
        } catch (e: IOException) {
            null
        }
    }

    private fun writeToLegacyDownloads(fileName: String, csvContent: String): Uri? {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        return try {
            FileOutputStream(file).use { it.write(csvContent.toByteArray()) }
            Uri.fromFile(file)
        } catch (e: IOException) {
            null
        }
    }
}
