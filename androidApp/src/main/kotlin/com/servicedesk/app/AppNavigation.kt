package com.servicedesk.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.servicedesk.app.presentation.auth.RegisterScreen
import com.servicedesk.app.presentation.auth.SignInScreen
import com.servicedesk.app.presentation.main.MainShell
import com.servicedesk.app.presentation.session.SessionStatus
import com.servicedesk.app.presentation.session.SessionViewModel

object MainRoutes {
    const val TICKETS = "tickets"
    const val DIALOGS = "dialogs"
    const val ANALYTICS = "analytics"
    const val MORE = "more"
}

object AuthRoutes {
    const val SIGN_IN = "signin"
    const val REGISTER = "register"
}

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel = hiltViewModel()) {
    val sessionStatus by sessionViewModel.status.collectAsStateWithLifecycle()

    when (sessionStatus) {
        SessionStatus.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        SessionStatus.SignedOut -> {
            val authNavController = rememberNavController()
            NavHost(
                navController = authNavController,
                startDestination = AuthRoutes.SIGN_IN,
            ) {
                composable(AuthRoutes.SIGN_IN) {
                    SignInScreen(
                        onNavigateToRegister = {
                            authNavController.navigate(AuthRoutes.REGISTER)
                        },
                        onSignedIn = sessionViewModel::onSignedIn,
                    )
                }
                composable(AuthRoutes.REGISTER) {
                    RegisterScreen(
                        onNavigateBack = { authNavController.popBackStack() },
                        onRegistered = sessionViewModel::onSignedIn,
                    )
                }
            }
        }

        SessionStatus.SignedIn -> MainShell(sessionViewModel = sessionViewModel)
    }
}
