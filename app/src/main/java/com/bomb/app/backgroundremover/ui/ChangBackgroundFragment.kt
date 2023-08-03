package com.bomb.app.backgroundremover.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.view.drawToBitmap
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bomb.app.backgroundremover.R
import com.bomb.app.backgroundremover.ads.Adsimpl
import com.bomb.app.backgroundremover.databinding.FragmentChangBackgroundBinding
import com.bomb.app.backgroundremover.utils.SaveImage
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class ChangBackgroundFragment : Fragment() {

    lateinit var binding: FragmentChangBackgroundBinding
    @Inject lateinit var saveImage: SaveImage
    @Inject lateinit var ads: Adsimpl
    var boolean =true
    private var GALLERY_REQUEST = 0

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chang_background, container, false)
         ads.loadnativad(binding.myTemplate)
        Picasso.get().load(requireArguments().getString("uri")!!.toUri()).into(binding.img)
        binding.bucolor.setOnClickListener { openbackgroundColorPicker() }
        binding.bugallery.setOnClickListener { PickImage() }
        binding.bu.setOnTouchListener { _, event ->
            binding.img.updateLayoutParams {
                width = event.rawX.toInt()
                height = event.rawY.toInt()
            }

            true
        }
        binding.img.setOnTouchListener { _, event ->
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    Toast.makeText(requireContext(), "ACTION_DOWN", Toast.LENGTH_SHORT).show()
                }
                MotionEvent.ACTION_MOVE -> {
                    binding.frame.x = event.rawX
                    binding.frame.y = event.rawY
                    Toast.makeText(requireContext(), "ACTION_MOVE", Toast.LENGTH_SHORT).show()

                }
            }
             true

        }
        binding.done.setOnClickListener {
            saveImage.savefile(saveImage.saveImageInQ(binding.allview.drawToBitmap()))
        }
        binding.background.setOnClickListener {
            hideandshow(boolean)
        }
        return binding.root
    }



    private fun hideandshow(boleen:Boolean) {
        if (boleen){
            binding.bu.visibility= INVISIBLE
            binding.card.strokeWidth=0
            boolean=false
        }else{
            binding.bu.visibility= VISIBLE
            binding.card.strokeWidth=4
            boolean=true
        }
    }


     fun openbackgroundColorPicker() {
        ColorPickerDialog.Builder(requireActivity())
            .setTitle("لون الخلفيه")
            .setPreferenceName("MyColorPickerDialog")
            .setPositiveButton("اختر",
                ColorEnvelopeListener { envelope, _ ->
                    binding.background.setImageDrawable(null);
                    binding.background.setBackgroundColor(envelope.color)

                })
            .setNegativeButton("الغاء"
            ) { dialogInterface, _ -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(true) // default is true. If false, do not show the AlphaSlideBar.
            .attachBrightnessSlideBar(true) // default is true. If false, do not show the BrightnessSlideBar.
            .show()



    }

    private fun PickImage() {
        val photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(Intent.createChooser(photoPickerIntent, "PICK_IMAGE"), GALLERY_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) when (requestCode) {
            GALLERY_REQUEST -> {
                val selectedImage = data?.data
                try {
//                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImage)
                    Picasso.get().load(selectedImage).into(binding.background)
                } catch (e: IOException) {
                    Log.i("TAG", "Some exception $e")
                }
            }
        }
    }


}


