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

// FIXME: This component should get string url of the image to display or uri if it from camera
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AvatarPicker(
    currentImageUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    size: Dp = 96.dp,
    showEditIcon: Boolean = true
) {
    val context = LocalContext.current

    // URI za privremenu sliku
    val imageUri = remember {
        val imageFile = File.createTempFile(
            "avatar_", ".jpg",
            context.cacheDir
        ).apply { createNewFile() }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            imageFile
        )
    }

    // Accompanist permission state
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    // Launcher za kameru
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImagePicked(imageUri)
        }
    }

    fun requestCameraAndLaunch() {
        if (cameraPermissionState.status.isGranted) {
            takePictureLauncher.launch(imageUri)
        } else {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.BottomEnd
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { requestCameraAndLaunch() },
            contentAlignment = Alignment.Center
        ) {
            if (currentImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(currentImageUri),
                    contentDescription = "Avatar",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add photo",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        // Edit dugme
        if (showEditIcon) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    .clickable { requestCameraAndLaunch() },
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
}
