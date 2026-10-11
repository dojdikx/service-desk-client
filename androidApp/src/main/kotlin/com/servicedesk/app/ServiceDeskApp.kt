package com.servicedesk.app

import android.app.Application
import com.servicedesk.app.locale.LocaleStoreEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class ServiceDeskApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val localeStore = EntryPointAccessors.fromApplication(this, LocaleStoreEntryPoint::class.java)
            .localeStore()
        runBlocking { localeStore.applyStoredLocale() }
    }
}
