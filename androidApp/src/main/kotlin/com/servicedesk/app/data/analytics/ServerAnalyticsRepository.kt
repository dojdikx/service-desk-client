package com.servicedesk.app.data.analytics

import com.servicedesk.app.BuildConfig
import com.servicedesk.app.data.api.AnalyticsApi
import com.servicedesk.app.data.api.AnalyticsRequestDto
import com.servicedesk.app.domain.analytics.AnalyticsQuery
import com.servicedesk.app.domain.analytics.AnalyticsReport
import com.servicedesk.app.domain.analytics.AnalyticsRepository
import com.servicedesk.app.domain.analytics.AnalyticsTicketRow
import com.servicedesk.app.domain.analytics.PrinterModelCount
import com.servicedesk.app.domain.analytics.StatusCount
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class ServerAnalyticsRepository @Inject constructor(
    private val api: AnalyticsApi,
    private val okHttpClient: OkHttpClient,
) : AnalyticsRepository {

    override suspend fun loadReport(query: AnalyticsQuery): AnalyticsReport {
        val dto = api.fetchReport(query.toDto(excel = false))
        return dto.toDomain()
    }

    override suspend fun downloadExcel(query: AnalyticsQuery, targetDir: File): File =
        withContext(Dispatchers.IO) {
            val excelPath = api.requestExcel(query.toDto(excel = true)).excelPath
            val url = buildUrl(excelPath)
            val request = Request.Builder().url(url).get().build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("Не удалось скачать файл (${response.code})")
                }
                val bytes = response.body?.bytes() ?: throw IOException("Пустой ответ")
                val fileName = excelPath.substringAfterLast('/').ifBlank { "analytics.xlsx" }
                val file = File(targetDir, fileName)
                file.writeBytes(bytes)
                file
            }
        }

    private fun AnalyticsQuery.toDto(excel: Boolean) = AnalyticsRequestDto(
        startDate = startDate,
        endDate = endDate,
        channels = listOf("max"),
        includeStatusBreakdown = includeStatusBreakdown,
        includePrinterModelsBreakdown = includePrinterModelsBreakdown,
        includeTicketsBreakdown = includeTicketsBreakdown,
        excel = excel,
    )

    private fun com.servicedesk.app.data.api.AnalyticsResponseDto.toDomain(): AnalyticsReport =
        AnalyticsReport(
            startDate = period.startDate,
            endDate = period.endDate,
            channels = period.channels,
            ticketsCreated = ticketsCreated,
            ticketsModified = ticketsModified,
            statusBreakdown = statusBreakdown?.map { StatusCount(it.status, it.count) } ?: emptyList(),
            printerModelBreakdown = printerModelBreakdown?.map {
                PrinterModelCount(it.printerModel, it.count)
            } ?: emptyList(),
            ticketsBreakdown = ticketsBreakdown?.map {
                AnalyticsTicketRow(
                    id = it.id,
                    status = it.status,
                    createdAt = it.createdAt,
                    printerModel = it.printerModel,
                    serialNumber = it.serialNumber,
                    reason = it.reason,
                )
            } ?: emptyList(),
        )

    private fun buildUrl(path: String): String {
        val base = BuildConfig.SERVER_BASE_URL.trimEnd('/')
        val normalized = if (path.startsWith("/")) path else "/$path"
        return base + normalized
    }

}
