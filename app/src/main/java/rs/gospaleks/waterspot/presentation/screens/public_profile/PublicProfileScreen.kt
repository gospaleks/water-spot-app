package rs.gospaleks.waterspot.presentation.screens.public_profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.BasicTopAppBar
import rs.gospaleks.waterspot.presentation.components.PointsChip
import rs.gospaleks.waterspot.presentation.components.RankBadge
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheet
import rs.gospaleks.waterspot.presentation.components.bottom_sheet.SpotDetailsBottomSheetViewModel
import rs.gospaleks.waterspot.presentation.components.card.SpotCard

@Composable
fun PublicProfileScreen(
    rootNavHostController: NavHostController,
    userId: String,
    onBackClick: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val bottomSheetViewModel: SpotDetailsBottomSheetViewModel = hiltViewModel()

    val isLoading = viewModel.uiState.isLoading
    val userWithSpots = viewModel.uiState.userWithspots
    val error = viewModel.uiState.error

    LaunchedEffect(userId) {
        viewModel.loadUserWithSpots(userId)
    }

    // Observe scroll to decide when to show the user's name in the top bar
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    // Approximate Y position of the name inside the hero section from the top: 28dp (top padding) + 100dp (avatar) + 12dp (spacing)
    val nameOffsetThresholdPx = with(density) { (28.dp + 100.dp + 12.dp + 24.dp).roundToPx() }
    val showUserTitle by remember {
        derivedStateOf {
            // When the first visible item is beyond the hero, or the hero is scrolled enough that the name would be under the top bar
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset >= nameOffsetThresholdPx
        }
    }

    Scaffold (
        topBar = {
            val titleText = when {
                userWithSpots != null && showUserTitle -> userWithSpots.user.fullName
                else -> stringResource(id = R.string.public_profile_title)
            }
            BasicTopAppBar(
                title = titleText,
                onBackClick = onBackClick
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator()
                }
                error != null -> {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                userWithSpots != null -> {
                    val spotsCount = userWithSpots.spots.size

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        state = listState
                    ) {
                        // Header (kept separate so everything below can have rounded top corners)
                        item {
                            HeroSection(
                                name = userWithSpots.user.fullName,
                                avatarUrl = userWithSpots.user.profilePictureUrl,
                                points = userWithSpots.user.points,
                            )
                        }

                        // Sheet-like container wrapping the rest of the content
                        item {
                            Surface(
                                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                                color = MaterialTheme.colorScheme.background,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    // Section header inside sheet
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp)
                                            .padding(top = 24.dp)
                                            .padding(bottom = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = stringResource(R.string.public_profile_spots_title),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.LocationOn,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    text = spotsCount.toString(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Clip
                                                )
                                            }
                                        }
                                    }

                                    if (userWithSpots.spots.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = stringResource(R.string.public_profile_no_spots),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    } else {
                                        Column(modifier = Modifier.fillMaxWidth()) {
                                            userWithSpots.spots.forEach { spot ->
                                                val spotWithUser = SpotWithUser(
                                                    spot = spot,
                                                    user = userWithSpots.user
                                                )
                                                SpotCard(
                                                    spotWithUser = spotWithUser,
                                                    onCardClick = {
                                                        bottomSheetViewModel.onSpotClick(spotWithUser)
                                                    },
                                                    onUserClick = {
                                                        // Already on author's profile
                                                    },
                                                    showAuthor = false,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(text = stringResource(R.string.public_profile_empty_state))
                }
            }
        }

        SpotDetailsBottomSheet(
            rootNavHostController = rootNavHostController,
            viewModel = bottomSheetViewModel,
            canClickOnAuthor = false
        )
    }
}

@Composable
private fun HeroSection(
    name: String,
    avatarUrl: String?,
    points: Int,
) {
    // Gradient hero background
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (!avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = name,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Initials
                    val initials = name.split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .take(2)
                        .joinToString("")
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Name
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )

            // Rank + Points
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RankBadge(points)
                PointsChip(points = points)
            }
        }
    }
}