package com.servicedesk.app.presentation.export

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.domain.export.TicketsExportQuery
import com.servicedesk.app.domain.export.TicketsExportRepository
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

data class TicketsExportUiState(
    val startDate: String = LocalDate.now().minusDays(30).toString(),
    val endDate: String = LocalDate.now().toString(),
    val loading: Boolean = false,
    val error: String? = null,
    val exportedFile: File? = null,
)

@HiltViewModel
class TicketsExportViewModel @Inject constructor(
    private val repository: TicketsExportRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TicketsExportUiState())
    val uiState: StateFlow<TicketsExportUiState> = _uiState.asStateFlow()

    fun onStartDateChange(value: String) {
        _uiState.update { it.copy(startDate = value) }
    }

    fun onEndDateChange(value: String) {
        _uiState.update { it.copy(endDate = value) }
    }

    fun export() {
        val start = parseDate(_uiState.value.startDate)
        val end = parseDate(_uiState.value.endDate)
        if (start == null || end == null) {
            _uiState.update { it.copy(error = "Укажите даты в формате ГГГГ-ММ-ДД") }
            return
        }
        if (start > end) {
            _uiState.update { it.copy(error = "Дата начала не может быть позже окончания") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, exportedFile = null) }
            val dir = File(context.cacheDir, "export").apply { mkdirs() }
            runCatching {
                repository.exportToFile(
                    TicketsExportQuery(start.toString(), end.toString()),
                    dir,
                )
            }
                .onSuccess { file -> _uiState.update { it.copy(loading = false, exportedFile = file) } }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(loading = false, error = e.message ?: "Не удалось выгрузить")
                    }
                }
        }
    }

    fun clearExportedFile() {
        _uiState.update { it.copy(exportedFile = null) }
    }

    private fun parseDate(raw: String): LocalDate? =
        try {
            LocalDate.parse(raw.trim())
        } catch (_: DateTimeParseException) {
            null
        }
}
