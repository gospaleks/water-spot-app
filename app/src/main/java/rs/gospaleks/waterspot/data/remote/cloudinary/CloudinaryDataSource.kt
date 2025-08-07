package rs.gospaleks.waterspot.data.remote.cloudinary

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.UploadCallback
import com.cloudinary.android.callback.ErrorInfo
import rs.gospaleks.waterspot.BuildConfig
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class CloudinaryDataSource @Inject constructor(
    private val mediaManager: MediaManager
) {
    suspend fun uploadAvatar(uri: Uri, fileName: String): String? = suspendCoroutine { cont ->

        val timestamp = System.currentTimeMillis() / 1000 // Keep as Long for consistency

        // Parameters that need to be signed (excluding file, cloud_name, resource_type, api_key)
        // According to Cloudinary docs: include all parameters except file, cloud_name, resource_type and api_key
        val paramsToSign = mapOf(
            "upload_preset" to "android_preset",
            "folder" to "avatars",
            "public_id" to fileName,
            "timestamp" to timestamp.toString() // Convert to string for signature
        )
        val signature = generateSignature(paramsToSign)

        mediaManager.upload(uri)
            .option("upload_preset", "android_preset")
            .option("resource_type", "image")
            .option("folder", "avatars")
            .option("public_id", fileName)
            .option("timestamp", timestamp)
            .option("api_key", "669639195878293")
            .option("signature", signature)
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

    private fun generateSignature(params: Map<String, String>): String {
        // Sort parameters alphabetically
        val sorted = params.toSortedMap()
        // Create the string to sign: param1=value1&param2=value2...
        val stringToSign = sorted.map { "${it.key}=${it.value}" }.joinToString("&")

        Log.d("CloudinaryDataSource", "String to sign: $stringToSign")

        // Append the API secret
        val stringWithSecret = "$stringToSign${BuildConfig.CLOUDINARY_API_SECRET}"
        return stringWithSecret.sha1()
    }

    private fun String.sha1(): String {
        val md = java.security.MessageDigest.getInstance("SHA-1")
        val bytes = md.digest(toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

}


// folder=avatars&overwrite=true&public_id=3GImSuVqE3exrFH85jvEbuaK7CK2&timestamp=1754530768