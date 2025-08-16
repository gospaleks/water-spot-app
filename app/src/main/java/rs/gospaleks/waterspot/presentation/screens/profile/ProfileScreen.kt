package rs.gospaleks.waterspot.presentation.screens.profile

import android.Manifest
import android.content.Intent
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import rs.gospaleks.waterspot.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import rs.gospaleks.waterspot.presentation.components.AvatarPicker
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import rs.gospaleks.waterspot.domain.model.AppTheme
import rs.gospaleks.waterspot.service.LocationTrackingService

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ProfileScreen(
    innerPadding: PaddingValues,
    onLogout: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMyWaterSpotsClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    themeViewModel: ThemeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // Notification permission state (Android 13+)
    val notificationsPermissionState: PermissionState? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    var pendingStartService by remember { mutableStateOf(false) }

    val selectedTheme by themeViewModel.appTheme.collectAsState()
    val nearbyRadius by viewModel.nearbyRadiusMeters.collectAsState()

    // Basic user data
    val fullName = viewModel.uiState.user.fullName
    val phoneNumber = viewModel.uiState.user.phoneNumber
    val userProfileImage = viewModel.uiState.user.profilePictureUrl

    // Loading states for profile data
    val isLoading = viewModel.uiState.isLoading
    val isAvatarUploading = viewModel.uiState.isAvatarUploading

    val isTrackingEnabled by viewModel.isTrackingEnabled.collectAsState()

    // LaunchedEffect za pokretanje/zaustavljanje servisa
    LaunchedEffect(Unit) {
        viewModel.startServiceEvent.collect {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && notificationsPermissionState?.status?.isGranted == false) {
                pendingStartService = true
                notificationsPermissionState.launchPermissionRequest()
            } else {
                val intent = Intent(context, LocationTrackingService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    // If user just granted notifications, start the service now
    LaunchedEffect(notificationsPermissionState?.status?.isGranted, pendingStartService) {
        if (pendingStartService && (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationsPermissionState?.status?.isGranted == true)) {
            pendingStartService = false
            val intent = Intent(context, LocationTrackingService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.stopServiceEvent.collect {
            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)
        }
    }

    // Reusable bottom sheet for each option
    var showBottomSheet by remember { mutableStateOf(false) }
    var currentSheetContent by remember {
        mutableStateOf<@Composable (ColumnScope.() -> Unit)?>(null)
    }

    ReusableBottomSheetHost(
        show = showBottomSheet,
        onDismissRequest = { showBottomSheet = false },
        sheetContent = {
            currentSheetContent?.invoke(this)
        }
    )

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
                        currentImageUri = null,
                        imageUrl = userProfileImage,
                        onImagePicked = { uri ->
                            viewModel.uploadAvatar(uri)
                        },
                        size = 96.dp,
                        isLoading = isAvatarUploading,
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
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.account_overview),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )

            Column (
                modifier = Modifier
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileOptionItem(
                    icon = Icons.Default.Person,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.edit_profile),
                    onClick = onEditProfileClick
                )
//                ProfileOptionItem(
//                    icon = Icons.Default.WaterDrop,
//                    iconTint = MaterialTheme.colorScheme.primary,
//                    title = stringResource(R.string.my_spots),
//                    onClick = onMyWaterSpotsClick
//                )
                ProfileOptionItem(
                    icon = Icons.Default.Lock,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.change_password),
                    onClick = onChangePasswordClick
                )
//                ProfileOptionItem(
//                    icon = Icons.Default.Translate,
//                    iconTint = MaterialTheme.colorScheme.primary,
//                    title = stringResource(R.string.change_language),
//                    onClick = {} // TODO: Implement language change with bottom sheet
//                )
                ProfileOptionItem(
                    icon = if (selectedTheme == AppTheme.DARK) Icons.Default.DarkMode else Icons.Default.LightMode,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.theme),
                    onClick = {
                        currentSheetContent = {
                            ThemeBottomSheetContent(
                                selectedTheme = selectedTheme,
                                onThemeSelected = {
                                    themeViewModel.onThemeSelected(it)
                                }
                            )
                        }
                        showBottomSheet = true
                    }
                )
                ProfileOptionItem(
                    icon = Icons.Default.Settings,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.settings),
                    onClick = {
                        currentSheetContent = {
                            SettingsBottomSheetContent(
                                checked = isTrackingEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.toggleLocationTracking(enabled)
                                },
                                radiusMeters = nearbyRadius,
                                onRadiusChange = { newRadius ->
                                    viewModel.setNearbyRadiusMeters(newRadius)
                                }
                            )
                        }
                        showBottomSheet = true
                    }
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
