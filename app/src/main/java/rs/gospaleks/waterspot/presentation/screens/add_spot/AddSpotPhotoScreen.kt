package rs.gospaleks.waterspot.presentation.screens.add_spot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.isGranted
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import rs.gospaleks.waterspot.presentation.screens.add_spot.components.PhotoUploadField

@Composable
fun AddSpotPhotoScreen(
    viewModel: AddSpotViewModel,
    onBackClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val uiState = viewModel.uiState

    Scaffold (
        topBar = {
            BasicTopAppBar(
                title = stringResource(id = R.string.add_spot_photo_title),
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = dimensionResource(R.dimen.padding_large))
        ) {
            Text(
                text = stringResource(R.string.add_spot_photo_instructions),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_extra_large))
            )

            // Photo upload section
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PhotoUploadField(
                    modifier = Modifier.fillMaxSize(),
                    photoUri = uiState.photoUri,
                    onPhotoSelected = { uri ->
                        viewModel.setPhotoUri(uri)
                    },
                    onPhotoRemoved = {
                        viewModel.setPhotoUri(null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))

            Button(
                onClick = onNextClick,
                enabled = uiState.photoUri != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = stringResource(R.string.add_spot_photo_confirmation),
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_extra_large)))
        }
    }
}