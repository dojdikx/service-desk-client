package com.servicedesk.app.presentation.analytics

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.domain.analytics.AnalyticsQuery
import com.servicedesk.app.domain.analytics.AnalyticsReport
import com.servicedesk.app.domain.analytics.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AnalyticsPhase {
    IDLE,
    LOADING,
    READY,
    ERROR,
}

private fun defaultStartDate(): String = LocalDate.now().minusDays(30).toString()
private fun defaultEndDate(): String = LocalDate.now().toString()

data class AnalyticsUiState(
    val startDate: String = defaultStartDate(),
    val endDate: String = defaultEndDate(),
    val includeStatusBreakdown: Boolean = true,
    val includePrinterModels: Boolean = true,
    val includeTickets: Boolean = false,
    val phase: AnalyticsPhase = AnalyticsPhase.IDLE,
    val error: String? = null,
    val report: AnalyticsReport? = null,
    val excelLoading: Boolean = false,
    val excelFile: File? = null,
    val excelError: String? = null,
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    fun onStartDateChange(value: String) {
        _uiState.update { it.copy(startDate = value) }
    }

    fun onEndDateChange(value: String) {
        _uiState.update { it.copy(endDate = value) }
    }

    fun onIncludeStatusChange(value: Boolean) {
        _uiState.update { it.copy(includeStatusBreakdown = value) }
    }

    fun onIncludePrinterChange(value: Boolean) {
        _uiState.update { it.copy(includePrinterModels = value) }
    }

    fun onIncludeTicketsChange(value: Boolean) {
        _uiState.update { it.copy(includeTickets = value) }
    }

    fun loadReport() {
        val query = buildQuery() ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    phase = AnalyticsPhase.LOADING,
                    error = null,
                    report = null,
                    excelFile = null,
                    excelError = null,
                )
            }
            runCatching { analyticsRepository.loadReport(query) }
                .onSuccess { report ->
                    _uiState.update {
                        it.copy(phase = AnalyticsPhase.READY, report = report)
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            phase = AnalyticsPhase.ERROR,
                            error = e.message ?: "Ошибка загрузки",
                        )
                    }
                }
        }
    }

    fun downloadExcel() {
        val query = buildQuery() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(excelLoading = true, excelError = null) }
            val dir = File(context.cacheDir, "analytics").apply { mkdirs() }
            runCatching { analyticsRepository.downloadExcel(query, dir) }
                .onSuccess { file ->
                    _uiState.update { it.copy(excelLoading = false, excelFile = file) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            excelLoading = false,
                            excelError = e.message ?: "Не удалось скачать Excel",
                        )
                    }
                }
        }
    }

    fun clearExcelFile() {
        _uiState.update { it.copy(excelFile = null) }
    }

    private fun buildQuery(): AnalyticsQuery? {
        val state = _uiState.value
        val start = parseDate(state.startDate)
        val end = parseDate(state.endDate)
        if (start == null || end == null) {
            _uiState.update {
                it.copy(
                    phase = AnalyticsPhase.ERROR,
                    error = "Укажите даты в формате ГГГГ-ММ-ДД",
                )
            }
            return null
        }
        if (start > end) {
            _uiState.update {
                it.copy(
                    phase = AnalyticsPhase.ERROR,
                    error = "Дата начала не может быть позже даты окончания",
                )
            }
            return null
        }
        return AnalyticsQuery(
            startDate = start.toString(),
            endDate = end.toString(),
            includeStatusBreakdown = state.includeStatusBreakdown,
            includePrinterModelsBreakdown = state.includePrinterModels,
            includeTicketsBreakdown = state.includeTickets,
        )
    }

    private fun parseDate(raw: String): LocalDate? =
        try {
            LocalDate.parse(raw.trim())
        } catch (_: DateTimeParseException) {
            null
        }

}
