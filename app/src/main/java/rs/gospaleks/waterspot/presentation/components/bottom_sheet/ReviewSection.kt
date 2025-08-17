package rs.gospaleks.waterspot.presentation.components.bottom_sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import rs.gospaleks.waterspot.domain.model.ReviewWithUser
import java.sql.Date
import java.text.SimpleDateFormat
import rs.gospaleks.waterspot.R

@Composable
fun ReviewsSection(
    reviews: List<ReviewWithUser>,
    isLoading: Boolean,
    onReviewerClick: (String) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when {
            isLoading -> repeat(4) { ReviewPlaceholder() }
            reviews.isEmpty() -> NoReviews()
            else -> reviews.forEach { reviewWithUser -> ReviewCard(reviewWithUser, onReviewerClick) }
        }
    }
}

@Composable
private fun NoReviews() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.RateReview,
                contentDescription = stringResource(R.string.spot_details_no_reviews),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Text(
                text = "No reviews yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewPlaceholder() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                )
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(14.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
            )
        }
    }
}

@Composable
private fun ReviewCard(reviewWithUser: ReviewWithUser, onReviewerClick: (String) -> Unit) {
    val review = reviewWithUser.review
    val user = reviewWithUser.user

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = user.profilePictureUrl,
                    contentDescription = "${user.fullName}'s profile picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onReviewerClick(user.id) },
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier.clickable { onReviewerClick(user.id) }
                ) {
                    Text(
                        text = user.fullName.ifBlank { "Anonymous" },
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(review.rating) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        review.createdAt?.let {
                            Text(
                                text = formatDate(it),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (review.comment.isNotBlank()) {
                Spacer(Modifier.height(8.dp))

                var expanded by remember { mutableStateOf(false) }
                val maxLines = if (expanded) Int.MAX_VALUE else 3

                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (review.comment.length > 100) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (expanded) stringResource(R.string.show_less) else stringResource(R.string.read_more),
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
    return sdf.format(Date(timestamp))
}
