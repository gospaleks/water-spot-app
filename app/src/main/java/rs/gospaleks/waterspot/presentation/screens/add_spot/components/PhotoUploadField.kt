package rs.gospaleks.waterspot.presentation.screens.add_spot.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.*
import rs.gospaleks.waterspot.R
import rs.gospaleks.waterspot.presentation.components.PhotoSourceDialog

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoUploadField(
    modifier: Modifier = Modifier,
    photoUri: Uri?,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit
) {
    var showSourceDialog by remember { mutableStateOf(false) }

    // UI
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showSourceDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(model = photoUri),
                contentDescription = "Selected photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            IconButton(
                onClick = { onPhotoRemoved() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove photo",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.add_spot_photo_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    PhotoSourceDialog(
        showDialog = showSourceDialog,
        onDismiss = { showSourceDialog = false },
        onImageSelected = {
            onPhotoSelected(it)
            showSourceDialog = false
        },
        tempFileNamePrefix = "spot_",
        justCamera = true,
    )
}
