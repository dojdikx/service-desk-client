package com.servicedesk.app.presentation.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.media.MediaUrlResolver
import com.servicedesk.app.di.ChatUiEntryPoint
import com.servicedesk.app.ui.theme.OnSurfaceMuted
import com.servicedesk.app.ui.theme.PrimaryBlue
import dagger.hilt.android.EntryPointAccessors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val chatUi = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, ChatUiEntryPoint::class.java)
    }
    val imageLoader = remember { chatUi.imageLoader() }
    val mediaUrlResolver = remember { chatUi.mediaUrlResolver() }
    val pickAttachment = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.sendPickedFile(context, uri)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
        bottomBar = {
            if (!uiState.isLoading && uiState.error == null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    IconButton(
                        onClick = { pickAttachment.launch("*/*") },
                        enabled = !uiState.isSending,
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null)
                    }
                    OutlinedTextField(
                        value = uiState.draft,
                        onValueChange = viewModel::onDraftChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Сообщение") },
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                    )
                    IconButton(
                        onClick = viewModel::send,
                        enabled = uiState.draft.isNotBlank() && !uiState.isSending,
                    ) {
                        if (uiState.isSending) {
                            CircularProgressIndicator(Modifier.padding(4.dp))
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        }
                    }
                }
            }
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null && uiState.messages.isEmpty() -> {
                Column(
                    modifier = Modifier.padding(padding).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::loadInitial) {
                        Text("Повторить")
                    }
                }
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF8FAFC)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.canLoadMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (uiState.isLoadingMore) {
                                    CircularProgressIndicator(Modifier.padding(8.dp))
                                } else {
                                    TextButton(onClick = viewModel::loadOlder) {
                                        Text("Ранние сообщения")
                                    }
                                }
                            }
                        }
                    }
                    items(uiState.messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            imageLoader = imageLoader,
                            mediaUrlResolver = mediaUrlResolver,
                        )
                    }
                }
            }
        }
    }
}

private fun deliveryStatusLabel(raw: String): String = when (raw.lowercase()) {
    "pending" -> "Отправляется…"
    "delivered", "sent" -> "Доставлено"
    "failed" -> "Ошибка доставки"
    else -> raw
}

@Composable
private fun MessageBubble(
    message: MessageDto,
    imageLoader: ImageLoader,
    mediaUrlResolver: MediaUrlResolver,
) {
    val fromStaff = message.messageType == "backend"
    val bubbleColor = if (fromStaff) PrimaryBlue else Color.White
    val textColor = if (fromStaff) Color.White else Color(0xFF0F172A)
    val align = if (fromStaff) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(bubbleColor, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Column {
                if (!message.quoteText.isNullOrBlank()) {
                    Text(
                        text = message.quoteText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (fromStaff) Color(0xFFE0E7FF) else OnSurfaceMuted,
                        modifier = Modifier.padding(bottom = 6.dp),
                    )
                }
                MessageMediaContent(
                    message = message,
                    imageLoader = imageLoader,
                    mediaUrlResolver = mediaUrlResolver,
                    modifier = Modifier.padding(bottom = if (message.messageText.isNotBlank()) 8.dp else 0.dp),
                )
                if (message.messageText.isNotBlank()) {
                    Text(
                        text = message.messageText,
                        color = textColor,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                val status = message.deliveryStatus?.let(::deliveryStatusLabel)
                if (fromStaff && !status.isNullOrBlank()) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE0E7FF),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}
