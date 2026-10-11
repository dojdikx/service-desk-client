package com.servicedesk.app.data.export

import com.servicedesk.app.BuildConfig
import com.servicedesk.app.data.api.TicketsExportApi
import com.servicedesk.app.data.api.TicketsExportRequestDto
import com.servicedesk.app.domain.export.TicketsExportQuery
import com.servicedesk.app.domain.export.TicketsExportRepository
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class ServerTicketsExportRepository @Inject constructor(
    private val api: TicketsExportApi,
    private val okHttpClient: OkHttpClient,
) : TicketsExportRepository {

    override suspend fun exportToFile(query: TicketsExportQuery, targetDir: File): File =
        withContext(Dispatchers.IO) {
            val excelPath = api.export(
                TicketsExportRequestDto(
                    startDate = query.startDate,
                    endDate = query.endDate,
                ),
            ).excelPath
            val url = buildUrl(excelPath)
            okHttpClient.newCall(Request.Builder().url(url).get().build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Не удалось скачать файл (${response.code})")
                }
                val bytes = response.body?.bytes() ?: throw IOException("Пустой ответ")
                val fileName = excelPath.substringAfterLast('/').ifBlank { "export.xlsx" }
                File(targetDir, fileName).apply { writeBytes(bytes) }
            }
        }

    private fun buildUrl(path: String): String {
        val base = BuildConfig.SERVER_BASE_URL.trimEnd('/')
        val normalized = if (path.startsWith("/")) path else "/$path"
        return base + normalized
    }
}
