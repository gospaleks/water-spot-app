package rs.gospaleks.waterspot.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import rs.gospaleks.waterspot.R

enum class UserRank(
    val minPoints: Int,
    val maxPoints: Int?,
    val color: Color,
    val icon: @Composable () -> Unit
) {
    Explorer(
        0, 19,
        Color(0xFF42A5F5),
        { Icon(Icons.Filled.Explore, contentDescription = "Explorer", tint = Color.White) }
    ),
    Contributor(
        20, 59,
        Color(0xFF66BB6A),
        { Icon(Icons.Filled.Star, contentDescription = "Contributor", tint = Color.White) }
    ),
    Scout(
        60, 99,
        Color(0xFFFFA726),
        { Icon(Icons.Filled.Search, contentDescription = "Scout", tint = Color.White) }
    ),
    Guide(
        100, 199,
        Color(0xFFAB47BC),
        { Icon(Icons.Filled.Map, contentDescription = "Guide", tint = Color.White) }
    ),
    WaterGuardian(
        200, null,
        Color(0xFFFFD700),
        { Icon(Icons.Filled.EmojiEvents, contentDescription = "Water Guardian", tint = Color.White) }
    );

    companion object {
        fun fromPoints(points: Int): UserRank {
            return entries.first { rank ->
                (points >= rank.minPoints) && (rank.maxPoints?.let { points <= it } ?: true)
            }
        }
    }
}

@Composable
fun UserRank.toDisplayName(): String = stringResource(
    when (this) {
        UserRank.Explorer -> R.string.rank_explorer
        UserRank.Contributor -> R.string.rank_contributor
        UserRank.Scout -> R.string.rank_scout
        UserRank.Guide -> R.string.rank_guide
        UserRank.WaterGuardian -> R.string.rank_water_guardian
    }
)

@Composable
fun RankBadge(
    points: Int,
    modifier: Modifier = Modifier
) {
    val rank = UserRank.fromPoints(points)

    // Rank chip
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = rank.color,
        contentColor = Color.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rank.icon()
            Text(
                text = rank.toDisplayName(),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

