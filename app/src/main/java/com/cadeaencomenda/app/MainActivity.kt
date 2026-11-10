package com.cadeaencomenda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.cadeaencomenda.app.data.preferences.PreferencesManager
import com.cadeaencomenda.app.data.preferences.ThemeMode
import com.cadeaencomenda.app.ui.navigation.AppNavigation
import com.cadeaencomenda.app.ui.navigation.Screen
import com.cadeaencomenda.app.ui.theme.CadeAEncomendaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val themeMode by preferencesManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val onboardingCompleted by preferencesManager.onboardingCompleted.collectAsState(initial = true)

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CadeAEncomendaTheme(darkTheme = darkTheme) {
                AppNavigation(
                    startDestination = if (onboardingCompleted) Screen.ROUTE_TRACKING_LIST else Screen.ROUTE_ONBOARDING
                )
            }
        }
    }
}
