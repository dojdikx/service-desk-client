package com.servicedesk.app.di

import com.servicedesk.app.data.auth.ServerStaffAuthRepository
import com.servicedesk.app.data.tickets.ServerTicketsRepository
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
}
