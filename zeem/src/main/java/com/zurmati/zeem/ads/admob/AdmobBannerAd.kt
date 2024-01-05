package com.zurmati.zeem.ads.admob

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.extensions.logEvent

class AdmobBannerAd {
    private var bannerCounter = 0

    private var adRequest = AdRequest.Builder().build()


    fun loadFreshBanner(
        activity: Activity,
        adId: String,
        container: FrameLayout,
        adSize: AdSize,
        listener: (Boolean) -> Unit = {}
    ) {
        if (GoogleBilling.isPremiumUser() || bannerCounter >= AdsManager.adData.BannerRequests)
            return

        loadAd(activity, adId, container, adSize, listener)
    }

    private fun loadAd(
        activity: Activity,
        adId: String,
        container: FrameLayout,
        bannerSize: AdSize = AdSize.BANNER,
        listener: (Boolean) -> Unit = {}
    ) {

        val adView = AdView(activity)

        adView.setAdSize(bannerSize)
        adView.adUnitId = adId

        adView.adListener = object : AdListener() {
            override fun onAdClicked() {
                logEvent("BannerAdClicked")
            }

            override fun onAdClosed() {

            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.i("LoadBannerAd", "onAdFailedToLoad: ${adError.message}")
                listener.invoke(false)
            }

            override fun onAdImpression() {
                logEvent("BannerAdImpression")
            }

            override fun onAdLoaded() {
                Log.i("LoadBannerAd", "onAdLoaded: Banner Loaded")

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