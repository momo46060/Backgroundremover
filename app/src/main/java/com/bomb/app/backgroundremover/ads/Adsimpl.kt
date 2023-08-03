package com.bomb.app.backgroundremover.ads

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import com.bomb.app.backgroundremover.R
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class Adsimpl  @Inject constructor(@ActivityContext private val contextt: Context)  {

   private var mInterstitialAd: InterstitialAd? = null

    fun preperad(): AdRequest {

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            contextt,
            contextt.resources.getString(R.string.intristl_id),
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    adError.toString().let { Log.d(TAG, it) }
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }
            })

        return AdRequest.Builder()
            .build()
    }

    fun showad() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(contextt as Activity)
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }
    }


    fun loadnativad(template: TemplateView) {
        val adLoader = AdLoader.Builder(contextt, "ca-app-pub-8270803172827285/4491871242")
            .forNativeAd { ad : NativeAd ->
                template.setNativeAd(ad)
                   template.updateLayoutParams {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }

            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                // Methods in the NativeAdOptions.Builder class can be
                // used here to specify individual options settings.
                .build())
            .build()
        adLoader.loadAd(AdRequest.Builder().build())


    }


}