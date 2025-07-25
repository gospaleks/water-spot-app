package rs.gospaleks.waterspot.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import rs.gospaleks.waterspot.presentation.navigation.AppNavHost
import rs.gospaleks.waterspot.presentation.ui.theme.WaterSpotTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            WaterSpotTheme {
                AppNavHost(
                    navController = navController
                )
            }
        }
    }
}