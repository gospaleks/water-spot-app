package rs.gospaleks.waterspot.presentation.screens.map.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import rs.gospaleks.waterspot.domain.model.SpotDetails
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.presentation.components.CleanlinessChip
import rs.gospaleks.waterspot.presentation.components.formatDate
import rs.gospaleks.waterspot.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotDetailsBottomSheet(
    spotDetails: SpotDetails? = null,
    isLoading: Boolean,
    selectedSpotId: String? = null,
    onDismiss: () -> Unit,
    onReportClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onLoadSpotDetails: (String) -> Unit = {},
    onUserProfileClick: () -> Unit = {}
) {
    LaunchedEffect(selectedSpotId) {
        selectedSpotId?.let { spotId ->
            // Mala pauza da se animacija otvaranja završi
            delay(200)
            onLoadSpotDetails(spotId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isLoading || spotDetails == null) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                SpotDetailsContent(
                    spotDetails = spotDetails,
                    onNavigateClick = onNavigateClick,
                    onReportClick = onReportClick,
                    onUserProfileClick = onUserProfileClick
                )
            }
        }
    }
}

@Composable
fun SpotDetailsContent(
    spotDetails: SpotDetails,
    onReportClick: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onUserProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Photo
        spotDetails.spot.photoUrl?.let { photoUrl ->
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Content section - redesigned layout

        // Header with type and date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = spotDetails.spot.type.icon(),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = spotDetails.spot.type.toDisplayName(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Creation date
            if (spotDetails.spot.createdAt != null) {
                Text(
                    text = formatDate(spotDetails.spot.createdAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cleanliness chip
        CleanlinessChip(
            cleanliness = spotDetails.spot.cleanliness,
            modifier = Modifier.wrapContentWidth()
        )

        // Description
        spotDetails.spot.description?.let { description ->
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.spot_details_description_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Clickable user info
        spotDetails.user?.let { user ->
            PostedByCard(user, onUserProfileClick)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action buttons
        ActionsButtons(
            onNavigateClick = onNavigateClick,
            onReportClick = onReportClick
        )
    }
}

@Composable
fun PostedByCard(
    user: User,
    onUserProfileClick: () -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        onClick = onUserProfileClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = user.profilePictureUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.spot_details_posted_by),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ActionsButtons(
    onNavigateClick: () -> Unit,
    onReportClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onNavigateClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Navigate")
        }

        OutlinedButton(
            onClick = onReportClick,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Report,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Report")
        }
    }
}