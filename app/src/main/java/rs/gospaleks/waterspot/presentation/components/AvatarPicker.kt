package rs.gospaleks.waterspot.presentation.components

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File

@Composable
fun AvatarPicker(
    currentImageUri: Uri?,
    onImagePicked: (Uri) -> Unit,
    size: Dp = 120.dp
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

    // Launcher za kameru
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImagePicked(imageUri)
        }
    }

    // Launcher za trazenje permisije
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(imageUri)
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(currentImageUri),
                contentDescription = "Avatar",
                modifier = Modifier
                    .matchParentSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Crop
            )
        } else {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Add photo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
