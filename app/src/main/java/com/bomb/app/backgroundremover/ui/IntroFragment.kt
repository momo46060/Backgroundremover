package com.bomb.app.backgroundremover.ui

import android.graphics.Matrix
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.aghajari.axanimation.AXAnimation
import com.bomb.app.backgroundremover.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntroFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       val view= inflater.inflate(R.layout.fragment_intro, container, false)
        Animation(view.findViewById(R.id.cardview))

        Handler().postDelayed({
            findNavController().navigate(IntroFragmentDirections.actionIntroFragmentToMainFragment())
        },3000)

        return view
    }
    private fun Animation(target: View) {
        val matrix = Matrix()
        matrix.setSkew(0.15f, 0.15f)
        matrix.postScale(1.5f, 1.5f)
        matrix.postTranslate(-100f, -100f)

        AXAnimation.create()
            .duration(1000)
            .matrix(matrix)
            .nextSectionWithDelay(500)
            .reversePreviousRule()
            .start(target)
    }

}