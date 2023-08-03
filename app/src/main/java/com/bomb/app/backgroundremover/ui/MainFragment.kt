package com.bomb.app.backgroundremover.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.bomb.app.backgroundremover.utils.AlertDialog
import com.bomb.app.backgroundremover.R
import com.bomb.app.backgroundremover.ads.Adsimpl
import com.bomb.app.backgroundremover.cutout.CutOut
import com.bomb.app.backgroundremover.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    @Inject lateinit var alertDialog: AlertDialog
    lateinit var currentPhotoPath: String
    @Inject lateinit var ads:Adsimpl
    private val REQUSTCODE =0
    private val PERMISSIONS = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false);
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 0)
        ads.preperad()
        ads.loadnativad(binding.myTemplate)
        binding.pickimage.setOnClickListener {
            ads.showad()
            findNavController().navigate(MainFragmentDirections.actionMainFragmentToBickImageFragment())
        }
        binding.camera.setOnClickListener {
            dispatchTakePictureIntent()

        }
        return binding.root
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.test.app.fileprovider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUSTCODE)

            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir: File? =
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image: File = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.absolutePath
        return image
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUSTCODE && resultCode ==  Activity.RESULT_OK ){
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val f = File(currentPhotoPath)
            val contentUri = Uri.fromFile(f)
            mediaScanIntent.data = contentUri
           alertDialog.show_alert()
            CoroutineScope(Main).launch{
                delay(5000)
                CutOut.activity()
                    .src(mediaScanIntent.data)
                    .intro()
                    .noCrop()
                    .start(requireActivity())
                    alertDialog.dialog.dismiss()
            }
        }

    }


}