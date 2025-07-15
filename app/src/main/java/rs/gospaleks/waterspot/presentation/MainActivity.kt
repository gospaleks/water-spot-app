package rs.gospaleks.waterspot.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.ui.theme.WaterSpotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WaterSpotTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Text(
                        text = "Welcome to WaterSpot",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(innerPadding)
                            .padding(dimensionResource(id = R.dimen.padding_large))
                    )
                }
            }
        }
    }
}