package com.bomb.app.backgroundremover.cutout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alexvasilkov.gestures.views.interfaces.GestureView
import com.bomb.app.backgroundremover.utils.AlertDialog
import com.bomb.app.backgroundremover.R
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.EasyImage.ImageSource
import top.defaults.checkerboarddrawable.CheckerboardDrawable
import java.io.File
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class CutOutActivity : AppCompatActivity() {
    @Inject lateinit var dialog: AlertDialog

    lateinit var loadingModal: FrameLayout
    private lateinit var gestureView: GestureView
    private lateinit var drawView: DrawView
    private lateinit var manualClearSettingsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_edit)
        val toolbar = findViewById<Toolbar>(R.id.photo_edit_toolbar)
        toolbar.setBackgroundColor(Color.BLACK)
        toolbar.setTitleTextColor(Color.WHITE)
        setSupportActionBar(toolbar)
        val drawViewLayout = findViewById<FrameLayout>(R.id.drawViewLayout)
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
            drawViewLayout.setBackgroundDrawable(CheckerboardDrawable.create())
        } else {
            drawViewLayout.background = CheckerboardDrawable.create()
        }
        val strokeBar = findViewById<SeekBar>(R.id.strokeBar)
        strokeBar.max = MAX_ERASER_SIZE.toInt()
        strokeBar.progress = 50
        gestureView = findViewById(R.id.gestureView)
        drawView = findViewById(R.id.drawView)
        drawView.setDrawingCacheEnabled(true)
        drawView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        //        drawView.setDrawingCacheEnabled(true);
        drawView.setStrokeWidth(strokeBar.progress)
        strokeBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                drawView.setStrokeWidth(seekBar.progress)
            }
        })
        loadingModal = findViewById(R.id.loadingModal)
        loadingModal.setVisibility(View.INVISIBLE)
        drawView.setLoadingModal(loadingModal)
        manualClearSettingsLayout = findViewById(R.id.manual_clear_settings_layout)
        setUndoRedo()
        initializeActionButtons()
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(false)
            if (toolbar.navigationIcon != null) {
                toolbar.navigationIcon!!.setColorFilter(
                    resources.getColor(R.color.white),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }
        val doneButton = findViewById<Button>(R.id.done)
        doneButton.setOnClickListener { v: View? ->
            CoroutineScope(Main).launch {
                dialog.show_alert()
                delay(5000)

                startSaveDrawingTask()

                dialog.dialog.dismiss()
            }

        }
        if (intent.getBooleanExtra(
                CutOut.CUTOUT_EXTRA_INTRO,
                false
            ) && !getPreferences(MODE_PRIVATE).getBoolean(
                INTRO_SHOWN, false
            )
        ) {
//            Intent intent = new Intent(this, IntroActivity.class);
//            startActivityForResult(intent, INTRO_REQUEST_CODE);

//            Intent intent = new Intent(this, IntroActivity.class);
//            startActivityForResult(intent, INTRO_REQUEST_CODE);

                    start()
        } else {
            start()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                setResult(RESULT_CANCELED)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private val extraSource: Uri?
        private get() = if (intent.hasExtra(CutOut.CUTOUT_EXTRA_SOURCE)) intent.getParcelableExtra<Parcelable>(
            CutOut.CUTOUT_EXTRA_SOURCE
        ) as Uri? else null

    private fun start() {
        val uri = extraSource
        Log.d("TAG", "onBindViewHolderonBindViewHolder:\$uri} $uri")
        if (intent.getBooleanExtra(CutOut.CUTOUT_EXTRA_CROP, false)) {
            var cropImageBuilder: CropImage.ActivityBuilder
            cropImageBuilder = if (uri != null) {
                CropImage.activity(uri)
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    CropImage.activity()
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                    return
                }
            }
            cropImageBuilder = cropImageBuilder.setGuidelines(CropImageView.Guidelines.ON)
            cropImageBuilder.start(this)
        } else {
            if (uri != null) {
                setDrawViewBitmap(uri)
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    EasyImage.openChooserWithGallery(
                        this,
                        "getString(R.string.image_chooser_message)",
                        IMAGE_CHOOSER_REQUEST_CODE
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun startSaveDrawingTask() {
        val task = SaveDrawingTask(this)
        var borderColor: Int
        if (intent.getIntExtra(CutOut.CUTOUT_EXTRA_BORDER_COLOR, -1)
                .also { borderColor = it } != -1
        ) {
            val image = BitmapUtility.getBorderedBitmap(
                drawView!!.drawingCache,
                borderColor,
                BORDER_SIZE.toInt()
            )
            task.execute(image)
        } else {
            task.execute(drawView!!.drawingCache)
        }
    }

    private fun activateGestureView() {
        gestureView!!.controller.settings
            .setMaxZoom(MAX_ZOOM)
            .setDoubleTapZoom(-1f) // Falls back to max zoom level
            .setPanEnabled(true)
            .setZoomEnabled(true)
            .setDoubleTapEnabled(true)
            .setOverscrollDistance(0f, 0f).overzoomFactor = 2f
    }

    private fun deactivateGestureView() {
        gestureView!!.controller.settings
            .setPanEnabled(false)
            .setZoomEnabled(false).isDoubleTapEnabled = false
    }

    private fun initializeActionButtons() {
        val autoClearButton = findViewById<Button>(R.id.auto_clear_button)
        val manualClearButton = findViewById<Button>(R.id.manual_clear_button)
        val zoomButton = findViewById<Button>(R.id.zoom_button)
        autoClearButton.isActivated = false
        autoClearButton.setOnClickListener { buttonView: View? ->
            if (!autoClearButton.isActivated) {
                drawView!!.setAction(DrawView.DrawViewAction.AUTO_CLEAR)
                manualClearSettingsLayout!!.visibility = View.INVISIBLE
                autoClearButton.isActivated = true
                manualClearButton.isActivated = false
                zoomButton.isActivated = false
                deactivateGestureView()
            }
        }
        manualClearButton.isActivated = true
        drawView!!.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
        manualClearButton.setOnClickListener { buttonView: View? ->
            if (!manualClearButton.isActivated) {
                drawView!!.setAction(DrawView.DrawViewAction.MANUAL_CLEAR)
                manualClearSettingsLayout!!.visibility = View.VISIBLE
                manualClearButton.isActivated = true
                autoClearButton.isActivated = false
                zoomButton.isActivated = false
                deactivateGestureView()
            }
        }
        zoomButton.isActivated = false
        deactivateGestureView()
        zoomButton.setOnClickListener { buttonView: View? ->
            if (!zoomButton.isActivated) {
                drawView!!.setAction(DrawView.DrawViewAction.ZOOM)
                manualClearSettingsLayout!!.visibility = View.INVISIBLE
                zoomButton.isActivated = true
                manualClearButton.isActivated = false
                autoClearButton.isActivated = false
                activateGestureView()
            }
        }
    }

    private fun setUndoRedo() {
        val undoButton = findViewById<Button>(R.id.undo)
        undoButton.isEnabled = false
        undoButton.setOnClickListener { v: View? -> undo() }
        val redoButton = findViewById<Button>(R.id.redo)
        redoButton.isEnabled = false
        redoButton.setOnClickListener { v: View? -> redo() }
        drawView!!.setButtons(undoButton, redoButton)
    }

    fun exitWithError(e: Exception?) {
        val intent = Intent()
        intent.putExtra(CutOut.CUTOUT_EXTRA_RESULT, e)
        setResult(CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE.toInt(), intent)
        finish()
    }

    private fun setDrawViewBitmap(uri: Uri) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            drawView!!.setBitmap(bitmap)
        } catch (e: IOException) {
            exitWithError(e)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                setDrawViewBitmap(result.uri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                exitWithError(result.error)
            } else {
                setResult(RESULT_CANCELED)
                finish()
            }
        } else if (requestCode == INTRO_REQUEST_CODE) {
            val editor = getPreferences(MODE_PRIVATE).edit()
            editor.putBoolean(INTRO_SHOWN, true)
            editor.apply()
            start()
        } else {
            EasyImage.handleActivityResult(
                requestCode,
                resultCode,
                data,
                this,
                object : DefaultCallback() {
                    override fun onImagePickerError(e: Exception, source: ImageSource, type: Int) {
                        exitWithError(e)
                    }

                    override fun onImagePicked(imageFile: File, source: ImageSource, type: Int) {
                        setDrawViewBitmap(Uri.parse(imageFile.toURI().toString()))
                    }

                    override fun onCanceled(source: ImageSource, type: Int) {
                        // Cancel handling, removing taken photo if it was canceled
                        if (source == ImageSource.CAMERA) {
                            val photoFile =
                                EasyImage.lastlyTakenButCanceledPhoto(this@CutOutActivity)
                            photoFile?.delete()
                        }
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                })
        }
    }

    private fun undo() {
        drawView!!.undo()
    }

    private fun redo() {
        drawView!!.redo()
    }

    companion object {
        private const val INTRO_REQUEST_CODE = 4
        private const val WRITE_EXTERNAL_STORAGE_CODE = 1
        private const val IMAGE_CHOOSER_REQUEST_CODE = 2
        private const val CAMERA_REQUEST_CODE = 3
        private const val INTRO_SHOWN = "INTRO_SHOWN"
        private const val MAX_ERASER_SIZE: Short = 150
        private const val BORDER_SIZE: Short = 45
        private const val MAX_ZOOM = 4f
    }
}