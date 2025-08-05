package rs.gospaleks.waterspot.presentation.screens.profile

import rs.gospaleks.waterspot.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import rs.gospaleks.waterspot.presentation.components.AvatarPicker

@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMyWaterSpotsClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val fullName = viewModel.uiState.userFullName
    val phoneNumber = viewModel.uiState.userPhoneNumber
    val userProfileImage = viewModel.uiState.userProfileImage
    val isLoading = viewModel.uiState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState())
    ) {
        // Header with Profile Photo, Name, and Phone Number
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                ProfileHeaderLoadingState(innerPadding)
            } else {
                Column(
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AvatarPicker(
                        currentImageUri = userProfileImage.toUri(),
                        onImagePicked = { /* TODO: implement avatar change  */ },
                        size = 96.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Account Overview List
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = dimensionResource(R.dimen.padding_large), vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.account_overview),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            ProfileOptionItem(
                icon = Icons.Default.Person,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.edit_profile),
                onClick = onEditProfileClick
            )
            ProfileOptionItem(
                icon = Icons.Default.LocationOn,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.my_spots),
                onClick = onMyWaterSpotsClick
            )
            ProfileOptionItem(
                icon = Icons.Default.Lock,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.change_password),
                onClick = onChangePasswordClick
            )
            ProfileOptionItem(
                icon = Icons.Default.Translate,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.change_language),
                onClick = {} // TODO: Implement language change with bottom sheet
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = stringResource(R.string.logout))
            }
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ProfileHeaderLoadingState(innerPadding: PaddingValues) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Ime placeholder
        Box(
            modifier = Modifier
                .height(20.dp)
                .width(160.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Telefon placeholder
        Box(
            modifier = Modifier
                .height(22.dp)
                .width(120.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f))
        )
    }
}


// Preview
@Composable
@Preview(showBackground = true)
fun ProfileScreenPreview() {
    ProfileScreen(
        innerPadding = PaddingValues(0.dp),
        onLogout = {},
        onEditProfileClick = {},
        onMyWaterSpotsClick = {},
        onChangePasswordClick = {},
    )
}
