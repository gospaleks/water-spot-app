package rs.gospaleks.waterspot.presentation.screens.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import rs.gospaleks.waterspot.domain.model.User
import rs.gospaleks.waterspot.presentation.components.RankBadge
import rs.gospaleks.waterspot.presentation.navigation.ProfileRouteScreen

@Composable
fun ScoreboardScreen(
    rootNavHostController: NavHostController,
    outerPadding: PaddingValues,
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    val userList = viewModel.uiState.users
    val isLoading = viewModel.uiState.isLoading
    val error = viewModel.uiState.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(outerPadding)
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = error, color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                val sorted = userList.sortedByDescending { it.points }
                val onUserClick: (User) -> Unit = { user ->
                    if (user.id.isNotBlank()) {
                        rootNavHostController.navigate(
                            ProfileRouteScreen.PublicProfile.createRoute(user.id)
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        PodiumSection(users = sorted.take(3), onUserClick = onUserClick)
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    }

                    itemsIndexed(sorted.drop(3), key = { index, user -> user.id.ifBlank { "pos_${index+4}" } }) { index, user ->
                        val position = index + 4 // since we dropped 3
                        LeaderboardRow(position = position, user = user, onClick = { onUserClick(user) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumSection(users: List<User>, onUserClick: (User) -> Unit) {
    if (users.isEmpty()) return

    val first = users.getOrNull(0)
    val second = users.getOrNull(1)
    val third = users.getOrNull(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // First place full width on top
        if (first != null) {
            PodiumCard(
                place = 1,
                user = first,
                avatarSize = 112.dp,
                cardHeight = 220.dp,
                accentColor = Color(0xFFFFD700), // Gold
                modifier = Modifier.fillMaxWidth(),
                onClick = { onUserClick(first) }
            )
        }

        Spacer(Modifier.height(12.dp))

        // Second and third side-by-side below
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                if (second != null) {
                    PodiumCard(
                        place = 2,
                        user = second,
                        avatarSize = 72.dp,
                        cardHeight = 180.dp,
                        accentColor = Color(0xFFC0C0C0), // Silver
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        onClick = { onUserClick(second) }
                    )
                }
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                if (third != null) {
                    PodiumCard(
                        place = 3,
                        user = third,
                        avatarSize = 72.dp,
                        cardHeight = 180.dp,
                        accentColor = Color(0xFFCD7F32), // Bronze
                        modifier = Modifier.fillMaxWidth(),
                        compact = true,
                        onClick = { onUserClick(third) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(
    place: Int,
    user: User,
    avatarSize: Dp,
    cardHeight: Dp,
    accentColor: Color,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .height(cardHeight)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (place == 1) 8.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Medal / place
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = accentColor
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "#$place",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            UserAvatar(
                imageUrl = user.profilePictureUrl,
                name = user.fullName,
                size = avatarSize
            )

            Text(
                text = user.fullName,
                style = if (compact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (compact) 2 else 1,
                textAlign = TextAlign.Center
            )

            PointsChip(points = user.points, compact = compact)
        }
    }
}

@Composable
private fun LeaderboardRow(position: Int, user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = position.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.width(32.dp)
        )

        UserAvatar(
            imageUrl = user.profilePictureUrl,
            name = user.fullName,
            size = 44.dp
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1
            )
            // Optional: you can show RankBadge below the name
            RankBadge(points = user.points, modifier = Modifier.padding(top = 4.dp))
        }

        PointsChip(points = user.points)
    }
}

@Composable
private fun PointsChip(points: Int, compact: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 4.dp else 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.WaterDrop,
                contentDescription = null,
                modifier = if (compact) Modifier.size(16.dp) else Modifier,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = points.toString(),
                style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun UserAvatar(imageUrl: String?, name: String, size: Dp) {
    val initials = name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("")
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}