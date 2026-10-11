package com.servicedesk.app.presentation.tickets

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.servicedesk.app.data.api.IssueTypesResponseDto
import com.servicedesk.app.data.api.TicketColumnDto
import com.servicedesk.app.data.api.TicketPatchRequestDto
import com.servicedesk.app.R
import com.servicedesk.app.domain.tickets.TicketsRepository
import com.servicedesk.app.locale.AppStrings
import com.servicedesk.app.locale.LocaleStore
import com.servicedesk.app.presentation.auth.toAuthMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailForm(
    val status: String = "",
    val fullName: String = "",
    val contactPhone: String = "",
    val reason: String = "",
    val printerModel: String = "",
    val serialNumber: String = "",
    val extraSerialNumber: String = "",
    val trackingNumber: String = "",
    val deliveryAddress: String = "",
    val purchasedFrom: String = "",
    val sku: String = "",
    val deliveryCost: String = "",
    val solutionDescription: String = "",
    val externalExportUrl: String = "",
    val serviceType: String = "",
    val serviceTime: String = "",
    val replenishmentRequirement: String = "",
    val serviceSolution: String = "",
    val warrantyStatus: String = "",
    val issueType: String = "",
    val subIssueType: String = "",
    val thirdIssueType: String = "",
    val qtyUsed: String = "",
    val replacementQty: String = "",
    val replenishmentQty: String = "",
    val skuRemark: String = "",
    val purchaseOrderId: String = "",
    val productType: String = "",
    val warrantyCase: Boolean = false,
)

data class TicketDetailUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val ticketId: Int = 0,
    val userId: Int = 0,
    val createdAt: String = "",
    val form: TicketDetailForm = TicketDetailForm(),
    val columns: List<TicketColumnDto> = emptyList(),
    val issueTypes: IssueTypesResponseDto? = null,
    val error: String? = null,
    val saveSuccess: Boolean = false,
)

@HiltViewModel
class TicketDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val ticketsRepository: TicketsRepository,
    private val localeStore: LocaleStore,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val ticketId: Int = savedStateHandle.get<Int>("ticketId")
        ?: savedStateHandle.get<String>("ticketId")?.toIntOrNull()
        ?: 0

    private val _uiState = MutableStateFlow(TicketDetailUiState(ticketId = ticketId))
    val uiState: StateFlow<TicketDetailUiState> = _uiState.asStateFlow()

    init {
        if (ticketId > 0) load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val ticket = ticketsRepository.getTicket(ticketId)
                val columns = ticketsRepository.loadColumns().columns
                val issueTypes = ticketsRepository.loadIssueTypes(localeStore.currentLanguage().apiLang)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userId = ticket.userId,
                        createdAt = ticket.createdAt,
                        form = ticket.toForm(),
                        columns = columns,
                        issueTypes = issueTypes,
                    )
                }
            }.onFailure { e ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.toAuthMessage(appStrings, R.string.ticket_detail_error_load),
                    )
                }
            }
        }
    }

    fun setEditing(editing: Boolean) {
        _uiState.update { it.copy(isEditing = editing) }
    }

    fun onFormChange(form: TicketDetailForm) {
        _uiState.update { it.copy(form = form, saveSuccess = false) }
    }

    fun save() {
        val form = _uiState.value.form
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            runCatching {
                ticketsRepository.patchTicket(
                    ticketId,
                    TicketPatchRequestDto(
                        status = form.status,
                        fullName = form.fullName,
                        contactPhone = form.contactPhone,
                        reason = form.reason,
                        printerModel = form.printerModel,
                        serialNumber = form.serialNumber,
                        extraSerialNumber = form.extraSerialNumber,
                        trackingNumber = form.trackingNumber,
                        deliveryAddress = form.deliveryAddress,
                        warrantyCase = form.warrantyCase,
                        purchasedFrom = form.purchasedFrom,
                        sku = form.sku,
                        deliveryCost = form.deliveryCost.toDoubleOrNull(),
                        solutionDescription = form.solutionDescription,
                        externalExportUrl = form.externalExportUrl,
                        serviceType = form.serviceType.takeIf { it.isNotBlank() },
                        serviceTime = form.serviceTime.takeIf { it.isNotBlank() },
                        replenishmentRequirement = form.replenishmentRequirement.takeIf { it.isNotBlank() },
                        serviceSolution = form.serviceSolution,
                        warrantyStatus = form.warrantyStatus.takeIf { it.isNotBlank() },
                        issueType = form.issueType.takeIf { it.isNotBlank() },
                        subIssueType = form.subIssueType.takeIf { it.isNotBlank() },
                        thirdIssueType = form.thirdIssueType.takeIf { it.isNotBlank() },
                        qtyUsed = form.qtyUsed.toIntOrNull(),
                        replacementQty = form.replacementQty.toIntOrNull(),
                        replenishmentQty = form.replenishmentQty.toIntOrNull(),
                        skuRemark = form.skuRemark,
                        purchaseOrderId = form.purchaseOrderId,
                        productType = form.productType,
                    ),
                )
            }
                .onSuccess { updated ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isEditing = false,
                            form = updated.toForm(),
                            saveSuccess = true,
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = e.toAuthMessage(appStrings, R.string.ticket_detail_error_save),
                        )
                    }
                }
        }
    }
}

private fun com.servicedesk.app.data.api.TicketDetailDto.toForm(): TicketDetailForm =
    TicketDetailForm(
        status = status,
        fullName = fullName ?: "",
        contactPhone = contactPhone ?: "",
        reason = reason ?: "",
        printerModel = printerModel ?: "",
        serialNumber = serialNumber ?: "",
        extraSerialNumber = extraSerialNumber ?: "",
        trackingNumber = trackingNumber ?: "",
        deliveryAddress = deliveryAddress ?: "",
        purchasedFrom = purchasedFrom ?: "",
        sku = sku ?: "",
        deliveryCost = deliveryCost?.toString() ?: "",
        solutionDescription = solutionDescription ?: "",
        externalExportUrl = externalExportUrl ?: "",
        serviceType = serviceType ?: "",
        serviceTime = serviceTime ?: "",
        replenishmentRequirement = replenishmentRequirement ?: "",
        serviceSolution = serviceSolution ?: "",
        warrantyStatus = warrantyStatus ?: "",
        issueType = issueType ?: "",
        subIssueType = subIssueType ?: "",
        thirdIssueType = thirdIssueType ?: "",
        qtyUsed = qtyUsed?.toString() ?: "",
        replacementQty = replacementQty?.toString() ?: "",
        replenishmentQty = replenishmentQty?.toString() ?: "",
        skuRemark = skuRemark ?: "",
        purchaseOrderId = purchaseOrderId ?: "",
        productType = productType ?: "",
        warrantyCase = warrantyCase == true,
    )
