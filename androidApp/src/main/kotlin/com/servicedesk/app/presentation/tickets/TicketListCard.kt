package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servicedesk.app.R
import com.servicedesk.app.data.api.StaffBriefDto
import com.servicedesk.app.data.api.TicketColumnDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.ui.theme.OnSurfaceMuted
import com.servicedesk.app.ui.theme.PrimaryBlue

@Composable
fun TicketListCard(
    ticket: TicketShortDto,
    columns: List<TicketColumnDto>,
    showAssignee: Boolean,
    staff: List<StaffBriefDto>,
    onOpenTicket: () -> Unit,
    onAssign: (Int) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenTicket),
            ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TicketAvatarInitials(name = ticket.clientName ?: "?")
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = ticket.clientName ?: stringResource(R.string.ticket_client_fallback),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (ticket.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = ticket.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                )
                            }
                        }
                    }
                    Text(
                        text = ticket.lastMessage ?: ticket.serialNumber ?: stringResource(R.string.text_em_dash),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
            TicketStatusPill(
                status = ticket.status,
                columns = columns,
                modifier = Modifier.padding(top = 12.dp),
            )
            }
            if (showAssignee) {
                TicketAssigneeField(
                    ticket = ticket,
                    staff = staff,
                    onAssign = onAssign,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }
    }
}

@Composable
fun TicketStatusPill(
    status: String,
    columns: List<TicketColumnDto>,
    modifier: Modifier = Modifier,
) {
    val column = columns.find { it.status == status }
    val background = column?.color?.let { parseHexColor(it) }?.copy(alpha = 0.18f)
        ?: Color(0xFFFEF3C7)
    val textColor = column?.color?.let { parseHexColor(it) } ?: Color(0xFF92400E)
    Text(
        text = status,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketAssigneeField(
    ticket: TicketShortDto,
    staff: List<StaffBriefDto>,
    onAssign: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = ticket.assigneeName ?: stringResource(R.string.ticket_assign_fallback)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (staff.isNotEmpty()) expanded = !expanded },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = assigneeFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            staff.forEach { member ->
                DropdownMenuItem(
                    text = { Text(member.displayName()) },
                    onClick = {
                        if (member.id != ticket.assignedTo) {
                            onAssign(member.id)
                        }
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun assigneeFieldColors(): TextFieldColors {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.background.luminance() < 0.5f
    val container = if (dark) scheme.surfaceVariant else Color(0xFFEFF6FF)
    val text = scheme.onSurface
    return OutlinedTextFieldDefaults.colors(
        focusedContainerColor = container,
        unfocusedContainerColor = container,
        disabledContainerColor = container,
        focusedTextColor = text,
        unfocusedTextColor = text,
        disabledTextColor = text,
        focusedTrailingIconColor = scheme.onSurfaceVariant,
        unfocusedTrailingIconColor = scheme.onSurfaceVariant,
        disabledTrailingIconColor = scheme.onSurfaceVariant,
    )
}

@Composable
private fun TicketAvatarInitials(name: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color(0xFFDBEAFE)),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = initials, color = PrimaryBlue, fontWeight = FontWeight.Bold)
    }
}

private fun parseHexColor(hex: String): Color {
    val normalized = hex.removePrefix("#")
    if (normalized.length != 6) return PrimaryBlue
    return runCatching {
        val rgb = normalized.toLong(16)
        Color(
            red = ((rgb shr 16) and 0xFF) / 255f,
            green = ((rgb shr 8) and 0xFF) / 255f,
            blue = (rgb and 0xFF) / 255f,
        )
    }.getOrDefault(PrimaryBlue)
}
