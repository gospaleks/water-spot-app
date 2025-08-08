package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.screens.all_spots.components.SpotCard

@Composable
fun AllSpotsScreen(
    outerPadding: PaddingValues,
    viewModel: AllSpotsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(outerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(uiState.spotsWithUser) { spotWithUser ->
                SpotCard(
                    spotWithUser = spotWithUser,
                    onUserClick = { userId ->
                        // TODO: Navigiraj do profila korisnika
                    },
                )
            }
        }
    }
}
