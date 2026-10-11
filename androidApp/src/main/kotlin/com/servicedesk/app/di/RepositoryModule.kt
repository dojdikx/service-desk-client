package com.servicedesk.app.di

import com.servicedesk.app.data.analytics.ServerAnalyticsRepository
import com.servicedesk.app.data.export.ServerTicketsExportRepository
import com.servicedesk.app.data.staff.ServerStaffAdminRepository
import com.servicedesk.app.data.auth.ServerStaffAuthRepository
import com.servicedesk.app.data.messages.ServerMessagesRepository
import com.servicedesk.app.data.tickets.ServerTicketsRepository
import com.servicedesk.app.domain.analytics.AnalyticsRepository
import com.servicedesk.app.domain.export.TicketsExportRepository
import com.servicedesk.app.domain.staff.StaffAdminRepository
import com.servicedesk.app.domain.messages.MessagesRepository
import com.servicedesk.app.domain.auth.StaffAuthRepository
import com.servicedesk.app.domain.tickets.TicketsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindStaffAuthRepository(
        impl: ServerStaffAuthRepository,
    ): StaffAuthRepository

    @Binds
    @Singleton
    abstract fun bindTicketsRepository(
        impl: ServerTicketsRepository,
    ): TicketsRepository

    @Binds
    @Singleton
    abstract fun bindMessagesRepository(
        impl: ServerMessagesRepository,
    ): MessagesRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: ServerAnalyticsRepository,
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindTicketsExportRepository(
        impl: ServerTicketsExportRepository,
    ): TicketsExportRepository

    @Binds
    @Singleton
    abstract fun bindStaffAdminRepository(
        impl: ServerStaffAdminRepository,
    ): StaffAdminRepository
}
