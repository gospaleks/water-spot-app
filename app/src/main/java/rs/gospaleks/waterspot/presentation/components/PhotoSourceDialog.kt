package rs.gospaleks.waterspot.presentation.components

import android.Manifest
import android.net.Uri
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import androidx.compose.ui.res.stringResource
import com.google.accompanist.permissions.isGranted
import rs.gospaleks.waterspot.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoSourceDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit,
    tempFileNamePrefix: String,
    permission: String = Manifest.permission.CAMERA
) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(permission = permission)

    val tempImageFile = remember {
        File.createTempFile(tempFileNamePrefix, ".jpg", context.cacheDir).apply {
            createNewFile()
        }
    }

    val imageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempImageFile
        )
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) onImageSelected(imageUri)
    }

    val pickFromGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.add_spot_photo_dialog_title)) },
            text = { Text(stringResource(R.string.add_spot_photo_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDismiss()
                        if (permissionState.status.isGranted) {
                            takePictureLauncher.launch(imageUri)
                        } else {
                            permissionState.launchPermissionRequest()
                        }
                    }
                ) {
                    Text(stringResource(R.string.add_spot_photo_camera_button))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        pickFromGalleryLauncher.launch("image/*")
                    }
                ) {
                    Text(stringResource(R.string.add_spot_photo_gallery_button))
                }
            }
        )
    }
}
