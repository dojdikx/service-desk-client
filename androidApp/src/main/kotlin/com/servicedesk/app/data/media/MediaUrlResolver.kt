package com.servicedesk.app.data.media

import com.servicedesk.app.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaUrlResolver @Inject constructor() {
    fun resolve(path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http://") || path.startsWith("https://")) return path
        val base = BuildConfig.SERVER_BASE_URL.trimEnd('/')
        val suffix = if (path.startsWith("/")) path else "/$path"
        return base + suffix
    }
}
