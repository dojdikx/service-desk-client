package com.servicedesk.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.servicedesk.app.locale.ActivityRecreator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var activityRecreator: ActivityRecreator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityRecreator.recreate = { recreate() }
        enableEdgeToEdge()
        setContent {
            AppNavigation()
        }
    }

    override fun onDestroy() {
        if (isFinishing) {
            activityRecreator.recreate = null
        }
        super.onDestroy()
    }
}
