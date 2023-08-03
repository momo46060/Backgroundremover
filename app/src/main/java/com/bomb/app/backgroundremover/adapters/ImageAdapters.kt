package com.bomb.app.backgroundremover.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bomb.app.backgroundremover.utils.AlertDialog
import com.bomb.app.backgroundremover.R
import com.bomb.app.backgroundremover.ads.Adsimpl
import com.bomb.app.backgroundremover.cutout.CutOut
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.Segmenter
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions.SINGLE_IMAGE_MODE
import com.squareup.picasso.Picasso
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject


@ActivityScoped
class ImageAdapters @Inject constructor(
    @ActivityContext private val context: Context,
    private val adsimpl: Adsimpl, private var alertDialog: AlertDialog
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var galleryList: List<String> = ArrayList()

    fun setlist(galleryList: List<String>) {
        this.galleryList = galleryList
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val menuItemLayoutView: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        ImageViewHolder(menuItemLayoutView)
        return ImageViewHolder(menuItemLayoutView)
    }
    private fun Perfoamauto(bitmapFromContentUri:Bitmap) {
        val client: Segmenter = Segmentation.getClient(SelfieSegmenterOptions.Builder().setDetectorMode(SINGLE_IMAGE_MODE).build())
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
                            Color.argb(((1.0 - d) * 255.0).toInt(), 0, 0, 0)
                        )
                    }
                }
                buffer.rewind()
                var autoeraseimage = overlay(bitmapFromContentUri, createBitmap)
                if (autoeraseimage != null) {
                    // Now set your auto eraseimagebitmap to your imageview
                    CutOut.activity()
                        .src(getImageUri(autoeraseimage))
                        .intro()
                        .noCrop()
                        .start(context as Activity)
                        alertDialog.dialog.dismiss()
                } else {
                    Toast.makeText(
                        context,
                        "resources.getString(R.string.please_try_again)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    fun getImageUri( inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }
    fun overlay(bmp1: Bitmap, bmp2: Bitmap): Bitmap? {
        val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
        val canvas = Canvas(bmOverlay)
        canvas.drawBitmap(bmp1, Matrix(), null)
        canvas.drawBitmap(bmp2, 0f, 0f, null)
        bmp1.recycle()
        bmp2.recycle()
        return bmOverlay
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {

            val path = File(galleryList[position])
            Picasso.get().load(path)
                .resize(300, 300).into(holder.imageView);
            holder.imageView.setOnClickListener {
                alertDialog.show_alert()

//                Perfoamauto(holder.imageView.drawToBitmap())
                 CoroutineScope(Main).launch {
                    delay(5000)
                    CutOut.activity()
                        .src(path.toUri())
                        .intro()
                        .noCrop()
                        .start(context as Activity)
                    adsimpl.showad()
                    alertDialog.dialog.dismiss()
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return galleryList.size
    }


    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView = itemView.findViewById(R.id.image)


    }


}
