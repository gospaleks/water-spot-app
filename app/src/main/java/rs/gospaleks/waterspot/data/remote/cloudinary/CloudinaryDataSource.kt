package rs.gospaleks.waterspot.data.remote.cloudinary

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CloudinaryDataSource @Inject constructor(
    private val mediaManager: MediaManager
) {
    suspend fun uploadAvatar(uri: Uri, fileName: String): String? = suspendCoroutine { cont ->
        mediaManager.upload(uri)
            .option("resource_type", "image")
            .option("folder", "avatars")
            .option("public_id", fileName)
            .unsigned("android_preset") // this preset does quality and size optimization
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val url = resultData?.get("secure_url") as? String
                    cont.resume(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    cont.resume(null)
                }
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
            })
            .dispatch()
    }

    // TODO: Implement delete function
}