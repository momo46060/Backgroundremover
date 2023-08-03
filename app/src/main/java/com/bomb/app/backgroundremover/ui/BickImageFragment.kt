package com.bomb.app.backgroundremover.ui

import android.Manifest
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bomb.app.backgroundremover.R
import com.bomb.app.backgroundremover.adapters.ImageAdapters
import com.bomb.app.backgroundremover.ads.Adsimpl
import com.bomb.app.backgroundremover.databinding.FragmentBickImageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint

class BickImageFragment : Fragment() {
    lateinit var binding: FragmentBickImageBinding
    @Inject lateinit var myadapter:ImageAdapters
    @Inject lateinit var ads:Adsimpl

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bick_image, container, false)
        ads.loadnativad(binding.myTemplate)
        binding.img.apply{
            setHasFixedSize(true)
            layoutManager=GridLayoutManager(requireContext(),2)
            adapter=myadapter
        }
            myadapter.setlist(fetchImages())

        return binding.root
    }
    fun fetchImages(): ArrayList<String> {
        val imageList: ArrayList<String> = ArrayList()
        val orderBy = MediaStore.Images.Thumbnails._ID + " DESC "

        val columns = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media._ID)
        val imagecursor: Cursor = requireActivity() .managedQuery(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            columns, null,
            null, orderBy
        )
;
        for (i in 0 until imagecursor.count) {
            imagecursor.moveToPosition(i)
            val dataColumnIndex =
                imagecursor.getColumnIndex(MediaStore.Images.Media.DATA)
            imageList.add(imagecursor.getString(dataColumnIndex))
        }
        Log.d("TAG", "onBindViewHolder: ${imageList.size}")
        return imageList
    }



}