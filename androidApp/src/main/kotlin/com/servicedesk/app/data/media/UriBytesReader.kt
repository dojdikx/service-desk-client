package com.servicedesk.app.data.media

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun Context.readUriBytes(uri: Uri): Triple<ByteArray, String, String> = withContext(Dispatchers.IO) {
    val mime = contentResolver.getType(uri) ?: "application/octet-stream"
    val name = queryDisplayName(uri) ?: "upload"
    val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
        ?: throw IllegalArgumentException("Не удалось прочитать файл")
    Triple(bytes, name, mime)
}

private fun Context.queryDisplayName(uri: Uri): String? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0 && cursor.moveToFirst()) {
            return cursor.getString(index)
        }
    }
    return uri.lastPathSegment
}
