package com.chibaminto.wearcost.ui

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Stores item images in app-private storage and exposes them through FileProvider URIs. */
internal object ImageStorage {
    private const val IMAGE_DIRECTORY = "item_images"
    private const val IMAGE_FILE_PREFIX = "wearcost_"
    private const val IMAGE_FILE_EXTENSION = ".jpg"

    fun copyFrom(context: Context, sourceUri: Uri): Uri? =
        runCatching {
            val imageFile = createImageFile(context)
            val inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                imageFile.delete()
                return@runCatching null
            }

            inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            uriForFile(context, imageFile)
        }.getOrNull()

    fun createCameraUri(context: Context): Uri =
        uriForFile(context, createImageFile(context))

    private fun createImageFile(context: Context): File {
        val imageDirectory = File(context.filesDir, IMAGE_DIRECTORY).apply { mkdirs() }
        return File(
            imageDirectory,
            "$IMAGE_FILE_PREFIX${System.currentTimeMillis()}$IMAGE_FILE_EXTENSION"
        )
    }

    private fun uriForFile(context: Context, imageFile: File): Uri =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
}
