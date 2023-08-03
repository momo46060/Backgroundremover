package com.bomb.app.backgroundremover

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.bomb.app.backgroundremover.ads.Adsimpl
import com.bomb.app.backgroundremover.cutout.CutOut
import com.bomb.app.backgroundremover.databinding.ActivityMainBinding
import com.bomb.app.backgroundremover.utils.SaveImage
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var adsimpl: Adsimpl
    lateinit var appOpenManager: MyApplication
    lateinit var binding: ActivityMainBinding
    lateinit var dialog: BottomSheetDialog
    private lateinit var navController: NavController
    @Inject
     lateinit var saveImage: SaveImage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Start()
    }


    private fun Start() {
        MobileAds.initialize(this) {}
        adsimpl.preperad()
        appOpenManager = MyApplication()
        navController = findNavController(R.id.nav)


    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        @Nullable data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CutOut.CUTOUT_ACTIVITY_REQUEST_CODE.toInt()) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val imageUri: Uri? = CutOut.getUri(data)
                    val b =Bundle()
                    b.putString("uri",imageUri.toString())
                    navController.navigate(R.id.changBackgroundFragment,b)

//                    saveImage.savefile(imageUri)
//                    val bitmap =
//                        MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
//                    saveImage.saveImageInQ(bitmap)
                }

                CutOut.CUTOUT_ACTIVITY_RESULT_ERROR_CODE.toInt() -> {
                    val ex: java.lang.Exception? = CutOut.getError(data)
                    Log.d("TAG", "onActivityResult:$ex ")

                }


                else -> Log.d("TAG", "onActivityResult:cancel ")

            }
        }
    }


    override fun onBackPressed() {
        if (navController.currentDestination!!.id == R.id.mainFragment) {
            bottomSheet()
        } else {
            super.onBackPressed()

        }
    }

    private fun bottomSheet() {
        val vie: View = layoutInflater.inflate(R.layout.back_bottom_sheet, null)
        dialog = BottomSheetDialog(this)
        dialog.setContentView(vie)
        adsimpl.loadnativad(vie.findViewById(R.id.my_template))
        val b = vie.findViewById<TextView>(R.id.i)
        b.setOnClickListener {
            finish()
        }
        dialog.show()
    }
}