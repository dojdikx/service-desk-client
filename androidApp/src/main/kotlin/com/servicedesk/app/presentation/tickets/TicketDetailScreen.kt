package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.servicedesk.app.data.api.IssueTypesResponseDto
import com.servicedesk.app.data.api.TicketColumnDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailScreen(
    onBack: () -> Unit,
    viewModel: TicketDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ticket_detail_title, uiState.ticketId)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (!uiState.isLoading && uiState.error == null) {
                        TextButton(onClick = { viewModel.setEditing(!uiState.isEditing) }) {
                            Text(
                                if (uiState.isEditing) {
                                    stringResource(R.string.ticket_detail_view)
                                } else {
                                    stringResource(R.string.ticket_detail_edit)
                                },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> CircularProgressIndicator(Modifier.padding(padding).padding(24.dp))
            uiState.error != null -> {
                Column(Modifier.padding(padding).padding(16.dp)) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::load, modifier = Modifier.padding(top = 8.dp)) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.ticket_detail_created, uiState.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (uiState.saveSuccess) {
                        Text(
                            text = stringResource(R.string.ticket_detail_saved),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    SectionTitle(stringResource(R.string.section_status))
                    StatusField(
                        enabled = uiState.isEditing,
                        value = uiState.form.status,
                        columns = uiState.columns,
                        onChange = { viewModel.onFormChange(uiState.form.copy(status = it)) },
                    )

                    SectionTitle(stringResource(R.string.section_contact))
                    FormField(stringResource(R.string.field_full_name), uiState.form.fullName, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(fullName = it))
                    }
                    FormField(stringResource(R.string.field_phone), uiState.form.contactPhone, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(contactPhone = it))
                    }
                    FormField(stringResource(R.string.field_reason), uiState.form.reason, uiState.isEditing, minLines = 3) {
                        viewModel.onFormChange(uiState.form.copy(reason = it))
                    }

                    SectionTitle(stringResource(R.string.section_printer))
                    FormField(stringResource(R.string.field_printer_model), uiState.form.printerModel, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(printerModel = it))
                    }
                    FormField(stringResource(R.string.field_serial), uiState.form.serialNumber, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(serialNumber = it))
                    }
                    FormField(stringResource(R.string.field_extra_serial), uiState.form.extraSerialNumber, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(extraSerialNumber = it))
                    }

                    SectionTitle(stringResource(R.string.section_delivery))
                    FormField(stringResource(R.string.field_tracking), uiState.form.trackingNumber, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(trackingNumber = it))
                    }
                    FormField(stringResource(R.string.field_delivery_address), uiState.form.deliveryAddress, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(deliveryAddress = it))
                    }
                    FormField(stringResource(R.string.field_purchased_from), uiState.form.purchasedFrom, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(purchasedFrom = it))
                    }
                    FormField(stringResource(R.string.field_sku), uiState.form.sku, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(sku = it))
                    }
                    FormField(stringResource(R.string.field_delivery_cost), uiState.form.deliveryCost, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(deliveryCost = it))
                    }
                    FormField(stringResource(R.string.field_solution), uiState.form.solutionDescription, uiState.isEditing, minLines = 2) {
                        viewModel.onFormChange(uiState.form.copy(solutionDescription = it))
                    }
                    RowSwitch(
                        label = stringResource(R.string.field_warranty_case),
                        checked = uiState.form.warrantyCase,
                        enabled = uiState.isEditing,
                        onChecked = { viewModel.onFormChange(uiState.form.copy(warrantyCase = it)) },
                    )

                    SectionTitle(stringResource(R.string.section_external_export))
                    FormField(stringResource(R.string.field_external_url), uiState.form.externalExportUrl, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(externalExportUrl = it))
                    }

                    SectionTitle(stringResource(R.string.section_excel_fields))
                    FormField(stringResource(R.string.field_service_type), uiState.form.serviceType, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(serviceType = it))
                    }
                    FormField(stringResource(R.string.field_service_time), uiState.form.serviceTime, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(serviceTime = it))
                    }
                    FormField(stringResource(R.string.field_replenishment), uiState.form.replenishmentRequirement, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(replenishmentRequirement = it))
                    }
                    FormField(stringResource(R.string.field_service_solution), uiState.form.serviceSolution, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(serviceSolution = it))
                    }
                    FormField(stringResource(R.string.field_warranty_status), uiState.form.warrantyStatus, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(warrantyStatus = it))
                    }
                    IssueTypeFields(
                        form = uiState.form,
                        issueTypes = uiState.issueTypes,
                        enabled = uiState.isEditing,
                        onFormChange = viewModel::onFormChange,
                    )
                    FormField(stringResource(R.string.field_qty_used), uiState.form.qtyUsed, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(qtyUsed = it))
                    }
                    FormField(stringResource(R.string.field_replacement_qty), uiState.form.replacementQty, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(replacementQty = it))
                    }
                    FormField(stringResource(R.string.field_replenishment_qty), uiState.form.replenishmentQty, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(replenishmentQty = it))
                    }
                    FormField(stringResource(R.string.field_sku_remark), uiState.form.skuRemark, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(skuRemark = it))
                    }
                    FormField(stringResource(R.string.field_order_id), uiState.form.purchaseOrderId, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(purchaseOrderId = it))
                    }
                    FormField(stringResource(R.string.field_product_type), uiState.form.productType, uiState.isEditing) {
                        viewModel.onFormChange(uiState.form.copy(productType = it))
                    }

                    if (uiState.isEditing) {
                        Button(
                            onClick = viewModel::save,
                            enabled = !uiState.isSaving,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                        ) {
                            if (uiState.isSaving) {
                                CircularProgressIndicator()
                            } else {
                                Text(stringResource(R.string.action_save))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    enabled: Boolean,
    minLines: Int = 1,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        enabled = enabled,
        readOnly = !enabled,
        minLines = minLines,
        singleLine = minLines == 1,
    )
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChecked, enabled = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusField(
    enabled: Boolean,
    value: String,
    columns: List<TicketColumnDto>,
    onChange: (String) -> Unit,
) {
    if (!enabled) {
        FormField(stringResource(R.string.section_status), value, false) {}
        return
    }
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.section_status)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            columns.forEach { column ->
                DropdownMenuItem(
                    text = { Text(column.status) },
                    onClick = {
                        onChange(column.status)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueTypeFields(
    form: TicketDetailForm,
    issueTypes: IssueTypesResponseDto?,
    enabled: Boolean,
    onFormChange: (TicketDetailForm) -> Unit,
) {
    val hierarchy = issueTypes?.hierarchy ?: emptyList()
    val labels = issueTypes?.labels ?: emptyMap()
    val subOptions = hierarchy.find { it.issueType == form.issueType }?.subIssueTypes ?: emptyList()

    IssueDropdown(
        label = "Issue type",
        value = form.issueType,
        options = hierarchy.map { it.issueType },
        labels = labels,
        enabled = enabled,
        onSelect = { onFormChange(form.copy(issueType = it, subIssueType = "", thirdIssueType = "")) },
    )
    IssueDropdown(
        label = "Sub issue type",
        value = form.subIssueType,
        options = subOptions,
        labels = labels,
        enabled = enabled && subOptions.isNotEmpty(),
        onSelect = { onFormChange(form.copy(subIssueType = it)) },
    )
    IssueDropdown(
        label = "Third issue type",
        value = form.thirdIssueType,
        options = issueTypes?.thirdIssueTypes ?: emptyList(),
        labels = labels,
        enabled = enabled,
        onSelect = { onFormChange(form.copy(thirdIssueType = it)) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IssueDropdown(
    label: String,
    value: String,
    options: List<String>,
    labels: Map<String, String>,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    if (!enabled) {
        val display = issueLabel(value, labels)
        FormField(label, display.ifBlank { value }, false) {}
        return
    }
    var expanded by remember { mutableStateOf(false) }
    val display = issueLabel(value, labels).ifBlank { value }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (options.isNotEmpty()) expanded = !expanded },
        modifier = Modifier.padding(bottom = 8.dp),
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(issueLabel(option, labels).ifBlank { option }) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun issueLabel(value: String, labels: Map<String, String>): String {
    if (value.isBlank()) return ""
    val key = value.lowercase().replace("[^a-z0-9]+".toRegex(), "_").trim('_')
    return labels[key] ?: value
}
