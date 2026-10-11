package com.servicedesk.app.locale

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRecreator @Inject constructor() {
    @Volatile
    var recreate: (() -> Unit)? = null
}
