package com.servicedesk.app.presentation.export

import android.content.Context
import com.servicedesk.app.R
import java.io.File

fun savedDownloadMessage(context: Context, file: File): String =
    context.getString(R.string.export_file_saved, file.absolutePath)
