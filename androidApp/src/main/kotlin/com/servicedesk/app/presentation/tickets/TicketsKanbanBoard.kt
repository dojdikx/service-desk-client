package com.servicedesk.app.presentation.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.servicedesk.app.R
import com.servicedesk.app.data.api.TicketColumnDto
import com.servicedesk.app.data.api.TicketShortDto
import com.servicedesk.app.ui.theme.OnSurfaceMuted
import com.servicedesk.app.ui.theme.PrimaryBlue

@Composable
fun TicketsKanbanBoard(
    columns: List<Pair<TicketColumnDto, List<TicketShortDto>>>,
    showAssignee: Boolean,
    onOpenTicket: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxHeight(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(columns, key = { it.first.id }) { (column, tickets) ->
            KanbanColumn(
                column = column,
                tickets = tickets,
                showAssignee = showAssignee,
                onOpenTicket = onOpenTicket,
            )
        }
    }
}

@Composable
private fun KanbanColumn(
    column: TicketColumnDto,
    tickets: List<TicketShortDto>,
    showAssignee: Boolean,
    onOpenTicket: (Int) -> Unit,
) {
    val accent = parseColumnColor(column.color)
    Column(
        modifier = Modifier
            .width(272.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accent),
            )
            Text(
                text = column.status,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            )
            Text(
                text = stringResource(R.string.ticket_kanban_count, tickets.size),
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceMuted,
            )
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 4.dp),
        ) {
            items(tickets, key = { it.ticketId }) { ticket ->
                KanbanTicketCard(
                    ticket = ticket,
                    showAssignee = showAssignee,
                    onClick = { onOpenTicket(ticket.ticketId) },
                )
            }
        }
    }
}

@Composable
private fun KanbanTicketCard(
    ticket: TicketShortDto,
    showAssignee: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ticket.clientName ?: stringResource(R.string.ticket_client_fallback),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (ticket.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = ticket.unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
            Text(
                text = ticket.lastMessage ?: ticket.reason ?: stringResource(R.string.text_em_dash),
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp),
            )
            if (showAssignee) {
                Text(
                    text = ticket.assigneeName ?: stringResource(R.string.ticket_assign_fallback),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (ticket.assigneeName != null) PrimaryBlue else OnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

private fun parseColumnColor(hex: String): Color {
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
