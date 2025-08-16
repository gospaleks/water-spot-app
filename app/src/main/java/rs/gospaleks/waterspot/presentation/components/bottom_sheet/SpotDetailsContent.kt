package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
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
    onUserProfileClick: () -> Unit = {}
) {
    val createdDate = remember(data.spot.createdAt) {
        data.spot.createdAt?.let { millis ->
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(millis))
        } ?: ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 📷 Image with gradient overlay and chips
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .border( // subtle outline around image card
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
                    SubcomposeAsyncImage(
                        model = data.spot.photoUrl,
                        contentDescription = "Spot photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f),
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
                                style = MaterialTheme.typography.labelMedium,
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
                                        style = MaterialTheme.typography.bodyMedium, // slightly bigger than labelSmall
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
                            isLoading = isLoading
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
                onReviewClick = onReviewClick
            )
        }
    }
}
