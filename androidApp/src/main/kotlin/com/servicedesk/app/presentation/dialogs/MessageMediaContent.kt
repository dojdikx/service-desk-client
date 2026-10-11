package com.servicedesk.app.presentation.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.servicedesk.app.R
import coil.compose.AsyncImage
import coil.ImageLoader
import com.servicedesk.app.data.api.MediaItemDto
import com.servicedesk.app.data.api.MessageDto
import com.servicedesk.app.data.media.MediaUrlResolver
import com.servicedesk.app.ui.theme.OnSurfaceMuted

@Composable
fun MessageMediaContent(
    message: MessageDto,
    imageLoader: ImageLoader,
    mediaUrlResolver: MediaUrlResolver,
    modifier: Modifier = Modifier,
) {
    val items = message.mediaItems?.takeIf { it.isNotEmpty() }
        ?: listOfNotNull(
            message.mediaType?.let { type ->
                message.mediaUrl?.let { url ->
                    MediaItemDto(
                        mediaType = type,
                        mediaUrl = url,
                        mediaFileName = message.mediaFileName,
                        mediaCaption = message.mediaCaption,
                    )
                }
            },
        )
    if (items.isEmpty()) return

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            when (item.mediaType) {
                "photo" -> {
                    val url = mediaUrlResolver.resolve(item.mediaUrl)
                    if (url != null) {
                        AsyncImage(
                            model = url,
                            imageLoader = imageLoader,
                            contentDescription = item.mediaFileName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 280.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
                "video" -> MediaFileRow(
                    icon = { Icon(Icons.Default.PlayCircle, contentDescription = null) },
                    title = item.mediaFileName ?: stringResource(R.string.media_video),
                )
                else -> MediaFileRow(
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    title = item.mediaFileName ?: stringResource(R.string.media_file),
                )
            }
            val caption = item.mediaCaption?.takeIf { it.isNotBlank() }
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun MediaFileRow(
    icon: @Composable () -> Unit,
    title: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon()
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceMuted)
    }
}
