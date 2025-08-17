package rs.gospaleks.waterspot.presentation.components.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.CleanlinessChip
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName

@Composable
fun SpotCard(
    spotWithUser: SpotWithUser,
    onCardClick: () -> Unit,
    onUserClick: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
    showAuthor: Boolean = true,
) {
    val spot = spotWithUser.spot
    val user = spotWithUser.user
    val createdDate = remember(spot.createdAt) {
        spot.createdAt?.let { millis ->
            java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
                .format(java.util.Date(millis))
        } ?: ""
    }

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 📷 Image with optional rating overlay
            Box(
                modifier = Modifier
                    .width(110.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.BottomStart
            ) {
                if (!spot.photoUrl.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = spot.photoUrl,
                        contentDescription = "Spot Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            )
                        },
                        error = {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                        }
                    )

                    // gradient overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                    startY = 120f
                                )
                            )
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "No Image",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                if (spot.averageRating > 0) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(spot.averageRating),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // 📄 Info section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Type + cleanliness
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = spot.type.icon(),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = spot.type.toDisplayName(),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    CleanlinessChip(spot.cleanliness)
                }

                Spacer(modifier = Modifier.weight(1f))

                // User + date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (showAuthor) {
                        user?.let {
                            if (!it.profilePictureUrl.isNullOrBlank()) {
                                coil.compose.AsyncImage(
                                    model = it.profilePictureUrl,
                                    contentDescription = "User Avatar",
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .clickable { onUserClick(user.id) },
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = it.fullName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, false)
                                    .clickable { onUserClick(user.id) }
                            )
                        }
                    }
                    if (createdDate.isNotEmpty()) {
                        Text(
                            text = if (showAuthor) "· $createdDate" else createdDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}