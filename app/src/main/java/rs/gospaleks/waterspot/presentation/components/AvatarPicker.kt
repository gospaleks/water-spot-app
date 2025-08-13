package rs.gospaleks.waterspot.presentation.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AvatarPicker(
    imageUrl: String?,
    currentImageUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    size: Dp = 96.dp,
    showEditIcon: Boolean = true,
    isLoading: Boolean = false
) {
    var showSourceDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.BottomEnd
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable(enabled = !isLoading) { showSourceDialog = true },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    strokeWidth = 2.dp
                )
            } else {
                when {
                    currentImageUri != null -> {
                        Image(
                            painter = rememberAsyncImagePainter(currentImageUri),
                            contentDescription = "Avatar",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    !imageUrl.isNullOrBlank() -> {
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Avatar",
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Add photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (showEditIcon && !isLoading) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clickable { showSourceDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit profile photo",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    // Prikaz dialoga za izbor izvora slike
    PhotoSourceDialog(
        showDialog = showSourceDialog,
        onDismiss = { showSourceDialog = false },
        onImageSelected = {
            onImagePicked(it)
        },
        tempFileNamePrefix = "avatar_"
    )
}