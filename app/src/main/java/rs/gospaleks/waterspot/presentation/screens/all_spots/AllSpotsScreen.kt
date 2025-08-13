package rs.gospaleks.waterspot.presentation.screens.all_spots

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import rs.gospaleks.waterspot.presentation.components.card.SpotCard
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheet
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheetViewModel

@Composable
fun AllSpotsScreen(
    outerPadding: PaddingValues,
    viewModel: AllSpotsViewModel = hiltViewModel()
) {
    val bottomSheetViewModel: SpotDetailsBottomSheetViewModel = hiltViewModel()

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
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.spotsWithUser) { spotWithUser ->
                SpotCard(
                    spotWithUser = spotWithUser,
                    onCardClick = {
                        bottomSheetViewModel.onSpotClick(spotWithUser)
                    },
                    onUserClick = { userId ->
                        // TODO: Navigiraj do profila korisnika
                    },
                )
            }
        }
    }

    SpotDetailsBottomSheet(viewModel = bottomSheetViewModel)
}
