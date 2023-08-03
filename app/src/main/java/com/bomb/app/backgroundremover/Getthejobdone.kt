package com.bomb.app.backgroundremover

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.widget.ImageView
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import dagger.hilt.android.scopes.ActivityScoped
import java.nio.ByteBuffer
import javax.inject.Inject

@ActivityScoped
class Getthejobdone @Inject constructor() {


     fun Perfoamauto(bitmapFromContentUri: Bitmap,view: ImageView) {
        val client: Segmenter = Segmentation.getClient(
            SelfieSegmenterOptions.Builder().setDetectorMode(
                SelfieSegmenterOptions.SINGLE_IMAGE_MODE
            ).build())
        client.process(InputImage.fromBitmap(bitmapFromContentUri, 0))
            .addOnSuccessListener { segmentationMask ->
                val buffer: ByteBuffer = segmentationMask!!.buffer
                val width: Int = segmentationMask.width
                val height: Int = segmentationMask.height
                val createBitmap = Bitmap.createBitmap(
                    bitmapFromContentUri.width,
                    bitmapFromContentUri.height,
                    bitmapFromContentUri.config
                )
                for (i in 0 until height) {
                    for (i2 in 0 until width) {
                        val d = buffer.float.toDouble()
                        java.lang.Double.isNaN(d)
                        createBitmap.setPixel(
                            i2,
                            i,
                            Color.TRANSPARENT
                        )
                    }
                }
                buffer.rewind()
                val autoeraseimage = overlay(bitmapFromContentUri, createBitmap)
                if (autoeraseimage != null) {
                    // Now set your auto eraseimagebitmap to your imageview
                    view.setImageBitmap(autoeraseimage)

                } else {
//                    Toast.makeText(
//                        requireContext(),
//                        "resources.getString(R.string.please_try_again)",
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
    }
    private fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap? {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        bmp1.recycle()
        bmp2.recycle()
        return bmOverlay
    }

}