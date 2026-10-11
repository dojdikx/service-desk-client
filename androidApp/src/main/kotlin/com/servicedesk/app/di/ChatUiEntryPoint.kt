package com.servicedesk.app.di

import coil.ImageLoader
import com.servicedesk.app.data.media.MediaUrlResolver
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ChatUiEntryPoint {
    fun imageLoader(): ImageLoader
    fun mediaUrlResolver(): MediaUrlResolver
}
