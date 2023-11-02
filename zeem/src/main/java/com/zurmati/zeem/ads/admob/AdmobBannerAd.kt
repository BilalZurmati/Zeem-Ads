package com.zurmati.zeem.ads.admob

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.extensions.logEvent

class AdmobBannerAd {
    private var bannerCounter = 0

    private var adRequest = AdRequest.Builder().build()


    fun loadFreshBanner(
        context: Context,
        adId: String,
        container: FrameLayout,
        listener: (Boolean) -> Unit = {}
    ) {
        if (bannerCounter >= AdsManager.adData.BannerRequests)
            return

        loadAd(context, adId, container, listener)
    }

    private fun loadAd(
        context: Context,
        adId: String,
        container: FrameLayout,
        listener: (Boolean) -> Unit = {}
    ) {

        val adView = AdView(context)
        adView.setAdSize(AdSize.BANNER)
        adView.adUnitId = adId

        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                logEvent("BannerAdClicked")
            }

            override fun onAdClosed() {

            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                listener.invoke(false)
            }

            override fun onAdImpression() {
                logEvent("BannerAdImpression")
            }

            override fun onAdLoaded() {
                listener.invoke(true)
                container.removeAllViews()
                container.addView(adView)
                container.visibility = View.VISIBLE
            }

            override fun onAdOpened() {
            }
        }



        adView.loadAd(adRequest)
        bannerCounter += 1
    }
}