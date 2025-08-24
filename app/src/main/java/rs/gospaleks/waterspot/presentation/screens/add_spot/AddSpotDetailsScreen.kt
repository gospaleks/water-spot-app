package rs.gospaleks.waterspot.presentation.screens.add_spot

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import rs.gospaleks.waterspot.presentation.components.UiEvent
import rs.gospaleks.waterspot.presentation.screens.add_spot.components.CleanlinessSelector
import rs.gospaleks.waterspot.presentation.screens.add_spot.components.SpotTypeSelector

@Composable
fun AddSpotDetailsScreen(
    viewModel: AddSpotViewModel,
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit,
) {
    val uiState = viewModel.uiState

    // Snackbar state and scope for showing error messages
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope ()
    val errorMessage = stringResource(R.string.add_spot_failed)

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.NavigateToHome -> {
                    onSubmitSuccess()
                }
                is UiEvent.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(message = errorMessage, withDismissAction = true)
                    }
                }
                else -> { }
            }
        }
    }

    Scaffold (
        topBar = {
            BasicTopAppBar(
                title = stringResource(id = R.string.add_spot_details_title),
                onBackClick = onBackClick
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionColor = MaterialTheme.colorScheme.secondary,
                    dismissActionContentColor = MaterialTheme.colorScheme.secondary
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, dimensionResource(R.dimen.padding_extra_large))
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = stringResource(R.string.add_spot_details_instructions),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(R.dimen.padding_extra_large))
            )

            // 1. Tip lokacije
            Text(
                text =  stringResource(R.string.add_spot_details_type_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SpotTypeSelector (
                selectedType = uiState.type,
                onTypeSelected = { viewModel.setType(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Čistoća
            Text(
                text = stringResource(R.string.add_spot_details_cleanliness_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            CleanlinessSelector (
                selected = uiState.cleanliness,
                onSelected = { viewModel.setCleanliness(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Opis (opciono)
            Text(
                text = stringResource(R.string.add_spot_details_description_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = uiState.description ?: "",
                onValueChange = { viewModel.setDescription(it) },
                placeholder = { Text(text = stringResource(R.string.add_spot_details_description_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier. weight(1f))

            Button(
                onClick = viewModel::submit,
                enabled = viewModel.canSubmitSpot() && !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.add_spot_details_confirmation),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}