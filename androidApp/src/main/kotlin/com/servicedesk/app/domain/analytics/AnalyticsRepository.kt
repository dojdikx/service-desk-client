package com.servicedesk.app.domain.analytics

import java.io.File

interface AnalyticsRepository {
    suspend fun loadReport(query: AnalyticsQuery): AnalyticsReport
    suspend fun downloadExcel(query: AnalyticsQuery, targetDir: File): File
}
