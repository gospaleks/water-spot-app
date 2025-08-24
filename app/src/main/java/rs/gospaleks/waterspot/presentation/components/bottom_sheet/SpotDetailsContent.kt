package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyListState
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import kotlinx.coroutines.launch
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.getColor
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName

@Composable
fun SpotDetailsContent(
    data: SpotWithUser,
    reviews: List<ReviewWithUser>,
    isLoading: Boolean,
    onReviewClick: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onUserProfileClick: () -> Unit = {},
    onAddPhotoClick: () -> Unit = {},
    onVisitClick: () -> Unit = {},
    isVisited: Boolean = false,
    isAddPhotoEnabled: Boolean = false,
    isUploadingPhoto: Boolean = false,
    onReviewerProfileClick: (String) -> Unit = {},
) {
    val createdDate = remember(data.spot.createdAt) {
        data.spot.createdAt?.let { millis ->
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(millis))
        } ?: ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Remember scroll across navigation using a stable key per spot
        val listState = rememberSaveable(data.spot.id, saver = LazyListState.Saver) { LazyListState() }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 📷 Image gallery (main photo + additional photos)
            item {
                val additional = data.spot.additionalPhotos
                val totalPhotos = 1 + additional.size
                val galleryState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                val atStart by remember { derivedStateOf { galleryState.firstVisibleItemIndex == 0 } }
                val atEnd by remember {
                    derivedStateOf {
                        val lastVisible = galleryState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisible >= totalPhotos - 1
                    }
                }
                // expand/collapse toggle state for entire gallery
                var expanded by remember { mutableStateOf(false) }
                val targetRatio = if (expanded) 3f / 4f else 4f / 3f
                val animatedRatio by animateFloatAsState(targetValue = targetRatio, label = "galleryAspect")

                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.animateContentSize()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LazyRow(
                            state = galleryState,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Main photo (keeps overlay and chips)
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .animateContentSize()
                                ) {
                                    SubcomposeAsyncImage(
                                        model = data.spot.photoUrl,
                                        contentDescription = "Spot photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .aspectRatio(animatedRatio),
                                        loading = {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            )
                                        },
                                        error = {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            )
                                        }
                                    )

                                    // Gradijent preko donjeg dela slike
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                                    startY = 200f
                                                )
                                            )
                                    )

                                    // Bottom-left expand/collapse toggle
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                            .clickable { expanded = !expanded }
                                    ) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                            contentDescription = if (expanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }

                                    // Chips u donjem desnom uglu slike
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Cleanliness chip
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(data.spot.cleanliness.getColor().copy(alpha = 0.85f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                imageVector = data.spot.cleanliness.icon(),
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.White
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = data.spot.cleanliness.toDisplayName(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White
                                            )
                                        }

                                        // Rating chip
                                        if (data.spot.averageRating > 0) {
                                            Row(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = "${"%.1f".format(data.spot.averageRating)} (${data.spot.reviewCount})",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Additional photos (if any)
                            items(additional) { spotPhoto ->
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .animateContentSize()
                                ) {
                                    SubcomposeAsyncImage(
                                        model = spotPhoto.url,
                                        contentDescription = "Additional photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillParentMaxWidth()
                                            .aspectRatio(animatedRatio),
                                        loading = {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                            )
                                        },
                                        error = {
                                            Box(
                                                Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                            )
                                        }
                                    )

                                    // Subtle gradient, no chips
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .background(
                                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.15f)),
                                                    startY = 220f
                                                )
                                            )
                                    )

                                    // Bottom-right date indicator
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        val photoDate = remember(spotPhoto.addedAt) {
                                            spotPhoto.addedAt?.let { millis ->
                                                java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                                                    .format(java.util.Date(millis.toDate().time))
                                            } ?: ""
                                        }
                                        Text(
                                            text = photoDate,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Bottom-left expand/collapse toggle (same global state)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                            .clickable { expanded = !expanded }
                                    ) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                                            contentDescription = if (expanded) "Collapse" else "Expand",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Chevrons remain unchanged
                        if (!atStart) {
                            Box(modifier = Modifier.matchParentSize()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .padding(start = 8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                        .clickable {
                                            val prev = (galleryState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                            scope.launch { galleryState.animateScrollToItem(prev) }
                                        },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Previous",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                        if (!atEnd) {
                            Box(modifier = Modifier.matchParentSize()) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                        .clickable {
                                            val next = (galleryState.firstVisibleItemIndex + 1).coerceAtMost(totalPhotos - 1)
                                            scope.launch { galleryState.animateScrollToItem(next) }
                                        },
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Next",
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Dot indicators
                    if (totalPhotos > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(totalPhotos) { index ->
                                val selected = galleryState.firstVisibleItemIndex == index
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(if (selected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // 🌍 Type + date & user (elevated container)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        // Type row (no chip)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = data.spot.type.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = data.spot.type.toDisplayName(),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )

                        // Date + user row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = createdDate,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(Modifier.width(8.dp))

                            data.user?.let { user ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .weight(1f, false)
                                        .clickable(onClick = onUserProfileClick)
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    AsyncImage(
                                        model = user.profilePictureUrl,
                                        contentDescription = "Profile picture",
                                        modifier = Modifier
                                            .size(28.dp) // larger touch target
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Text(
                                        text = user.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 📝 Description with header (elevated container)
            data.spot.description?.takeIf { it.isNotBlank() }?.let { description ->
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ⭐ Reviews
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Reviews",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            thickness = 0.5.dp
                        )
                        ReviewsSection(
                            reviews = reviews,
                            isLoading = isLoading,
                            onReviewerClick = onReviewerProfileClick,
                        )
                    }
                }
            }
        }

        // 📍 Bottom buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            ActionsButtons(
                onNavigateClick = onNavigateClick,
                onReviewClick = onReviewClick,
                onAddPhotoClick = onAddPhotoClick,
                onVisitClick = onVisitClick,
                isVisited = isVisited,
                isAddPhotoEnabled = isAddPhotoEnabled,
                isUploadingPhoto = isUploadingPhoto,
            )
        }
    }
}
