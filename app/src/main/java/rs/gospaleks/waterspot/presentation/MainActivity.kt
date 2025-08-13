package rs.gospaleks.waterspot.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import rs.gospaleks.waterspot.domain.model.AppTheme
import rs.gospaleks.waterspot.presentation.navigation.graphs.RootNavGraph
import rs.gospaleks.waterspot.presentation.screens.profile.ThemeViewModel
import rs.gospaleks.waterspot.presentation.ui.theme.WaterSpotTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
        )
        setContent {
            val theme by themeViewModel.appTheme.collectAsState()

            val isDark = when (theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            WaterSpotTheme(darkTheme = isDark) {
                RootNavGraph()
            }
        }
    }
}