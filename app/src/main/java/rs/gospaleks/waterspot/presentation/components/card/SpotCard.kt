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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import rs.gospaleks.waterspot.domain.model.SpotWithUser
import rs.gospaleks.waterspot.presentation.components.CleanlinessChip
import rs.gospaleks.waterspot.presentation.components.icon
import rs.gospaleks.waterspot.presentation.components.toDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
    val updatedDate = spot.updatedAt?.let { millis ->
        formatRelativeOrDate(millis)
    } ?: ""

    // Precompute target pixel sizes for images to avoid decoding huge bitmaps
    val density = LocalDensity.current
    val context = LocalContext.current
    val avatarPx = with(density) { 22.dp.roundToPx() } to with(density) { 22.dp.roundToPx() }
    val imageSizeDp = 110.dp
    val imagePx = with(density) { imageSizeDp.roundToPx() } to with(density) { imageSizeDp.roundToPx() }

    Card(
        onClick = onCardClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageSizeDp)
        ) {
            // 📷 Image with loading/error and subtle gradient
            Box(
                modifier = Modifier
                    .width(imageSizeDp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.BottomStart
            ) {
                if (!spot.photoUrl.isNullOrEmpty()) {
                    SubcomposeAsyncImage(
                        model = remember(spot.photoUrl) {
                            ImageRequest.Builder(context)
                                .data(spot.photoUrl)
                                .size(imagePx.first, imagePx.second)
                                .crossfade(true)
                                .build()
                        },
                        contentDescription = "Spot photo",
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(strokeWidth = 2.dp)
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = "Image unavailable",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f)
                    )

                    // gradient overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.32f)),
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
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

                // Subtle, single-line description preview (char-limited)
                val desc = spot.description?.trim().orEmpty()
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                                AsyncImage(
                                    model = remember(it.profilePictureUrl) {
                                        ImageRequest.Builder(context)
                                            .data(it.profilePictureUrl)
                                            .size(avatarPx.first, avatarPx.second)
                                            .crossfade(true)
                                            .build()
                                    },
                                    contentDescription = "User avatar",
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .clickable { onUserClick(it.id) },
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Monogram fallback
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { onUserClick(it.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (it.fullName.firstOrNull()?.uppercase() ?: "?"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = it.fullName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f, false)
                                    .clickable { onUserClick(it.id) }
                            )
                        }
                    }
                    if (updatedDate.isNotEmpty()) {
                        Text(
                            text = if (showAuthor) "· $updatedDate" else updatedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatRelativeOrDate(millis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - millis
    val absDiff = abs(diff)
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day

    return when {
        absDiff < minute -> "just now"
        absDiff < hour -> "${absDiff / minute}m ago"
        absDiff < day -> "${absDiff / hour}h ago"
        absDiff < week -> "${absDiff / day}d ago"
        absDiff < 30 * day -> "${absDiff / week}w ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(millis))
    }
}
