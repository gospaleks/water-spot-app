package rs.gospaleks.waterspot.presentation.components.card

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)

    return Brush.linearGradient(
        colors = listOf(base.copy(alpha = 0.6f), highlight, base.copy(alpha = 0.6f)),
        start = Offset(translate - 200f, 0f),
        end = Offset(translate, 0f)
    )
}

@Composable
fun ShimmerSpotCardPlaceholder() {
    val shimmer = rememberShimmerBrush()
    val shape = RoundedCornerShape(16.dp)
    val imageShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .height(110.dp)
    ) {
        // Left image placeholder
        Box(
            modifier = Modifier
                .width(110.dp)
                .fillMaxHeight()
                .clip(imageShape)
                .background(shimmer)
        )

        Spacer(Modifier.width(12.dp))

        // Right content placeholders
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 14.dp)
                .weight(1f)
        ) {
            // Title row placeholder
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .width(70.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Second line placeholder
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.7f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(shimmer)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer row placeholder
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(100.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
                Box(
                    modifier = Modifier
                        .height(12.dp)
                        .width(60.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }
        }

        Spacer(Modifier.width(12.dp))
    }
}
