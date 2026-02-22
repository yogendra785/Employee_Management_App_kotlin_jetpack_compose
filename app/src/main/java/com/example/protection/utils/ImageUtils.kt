package com.example.protection.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {

    private const val MAX_WIDTH = 800
    private const val MAX_HEIGHT = 800
    private const val COMPRESSION_QUALITY = 80 // 0 (low) to 100 (high)

    /**
     * Compresses the image at the given URI to a ByteArray (JPEG).
     * It resizes the image to fit within 800x800 and compresses quality to 80%.
     */
    fun compressImage(context: Context, imageUri: Uri): ByteArray? {
        return try {
            // 1. Calculate the correct scale factor (Sample Size)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true // Only read dimensions, don't load data yet
            }
            context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT)
            options.inJustDecodeBounds = false // Now load the actual image

            // 2. Decode the bitmap with the scale factor
            val bitmap = context.contentResolver.openInputStream(imageUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            // 3. Compress to JPEG ByteArray
            bitmap?.let { bmp ->
                val stream = ByteArrayOutputStream()
                bmp.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, stream)
                bmp.recycle() // Free up memory immediately
                stream.toByteArray()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Helper to calculate how much to shrink the image
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}