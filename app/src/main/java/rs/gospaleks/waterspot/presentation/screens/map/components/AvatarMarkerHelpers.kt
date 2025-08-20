package rs.gospaleks.waterspot.presentation.screens.map.components

import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.toColorInt
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// --- Helpers for circular avatar map markers ---
fun extractInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar()
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar()
    return when {
        first != null && second != null -> "$first$second"
        first != null -> first.toString()
        else -> "?"
    }
}

// Synchronous placeholder (no network): initials on a colored circle with border
fun createInitialsAvatarBitmap(
    name: String,
    sizePx: Int,
): Bitmap {
    val output = createBitmap(sizePx, sizePx)
    val canvas = Canvas(output)
    val radius = sizePx / 2f

    // Background circle fill
    val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#2196F3".toColorInt() // blue-ish
        style = Paint.Style.FILL
    }
    canvas.drawCircle(radius, radius, radius, circlePaint)

    // Initials text
    val initials = extractInitials(name)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = sizePx * 0.42f
    }
    val textBounds = android.graphics.Rect()
    textPaint.getTextBounds(initials, 0, initials.length, textBounds)
    val textY = radius - textBounds.exactCenterY()
    canvas.drawText(initials, radius, textY, textPaint)

    // White border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.06f
    }
    val inset = borderPaint.strokeWidth / 2
    canvas.drawArc(RectF(inset, inset, sizePx - inset, sizePx - inset), 0f, 360f, false, borderPaint)

    return output
}

suspend fun createCircularAvatarBitmap(
    context: android.content.Context,
    imageUrl: String?,
    name: String,
    sizePx: Int,
): Bitmap {
    val loader = context.imageLoader
    val drawableBitmap: Bitmap? = try {
        if (!imageUrl.isNullOrBlank()) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .size(sizePx)
                .allowHardware(false)
                .build()
            val result = loader.execute(request)
            val drawable = (result as? SuccessResult)?.drawable
            drawable?.toBitmap(width = sizePx, height = sizePx)
        } else null
    } catch (_: Exception) {
        null
    }

    // Prepare paints
    val output = createBitmap(sizePx, sizePx)
    val canvas = Canvas(output)
    val radius = sizePx / 2f

    // Background circle (slight gray)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = "#F0F0F0".toColorInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(radius, radius, radius, bgPaint)

    if (drawableBitmap != null) {
        val scaled = if (drawableBitmap.width != sizePx || drawableBitmap.height != sizePx) {
            drawableBitmap.scale(sizePx, sizePx)
        } else drawableBitmap

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawCircle(radius, radius, radius, paint)
    } else {
        // Fallback: initials
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#2196F3".toColorInt() // blue-ish
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius, circlePaint)

        val initials = extractInitials(name)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            textSize = sizePx * 0.42f
        }
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(initials, 0, initials.length, textBounds)
        val textY = radius - textBounds.exactCenterY()
        canvas.drawText(initials, radius, textY, textPaint)
    }

    // Border
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.06f
    }
    // Inset a bit so stroke is fully inside
    val inset = borderPaint.strokeWidth / 2
    canvas.drawArc(RectF(inset, inset, sizePx - inset, sizePx - inset), 0f, 360f, false, borderPaint)

    return output
}

@Composable
fun rememberAvatarMarkerDescriptor(
    imageUrl: String?,
    name: String,
    size: Dp = 48.dp,
): BitmapDescriptor {
    val context = LocalContext.current
    val density = LocalDensity.current
    // Compute size first to build an immediate placeholder
    val sizePx = with(density) { size.roundToPx() }

    // Create a non-null placeholder immediately to avoid default red marker flicker
    val initialPlaceholder = remember(name, sizePx) {
        BitmapDescriptorFactory.fromBitmap(createInitialsAvatarBitmap(name, sizePx))
    }

    val (descriptor, setDescriptor) = remember(imageUrl, name, sizePx) {
        mutableStateOf<BitmapDescriptor>(initialPlaceholder)
    }

    LaunchedEffect(imageUrl, name, sizePx) {
        val bmp = createCircularAvatarBitmap(context, imageUrl, name, sizePx)
        setDescriptor(BitmapDescriptorFactory.fromBitmap(bmp))
    }

    return descriptor
}