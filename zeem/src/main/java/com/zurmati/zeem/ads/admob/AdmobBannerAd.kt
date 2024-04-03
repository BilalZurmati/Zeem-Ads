package com.zurmati.zeem.ads.admob

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.enums.BANNER
import com.zurmati.zeem.extensions.logEvent
import com.zurmati.zeem.utils.Utils
import java.util.UUID

class AdmobBannerAd {
    private var bannerCounter = 0


    fun loadFreshBanner(
        activity: Activity,
        adId: String,
        container: FrameLayout,
        adSize: BANNER,
        listener: (Boolean) -> Unit = {}
    ) {
        if (!Utils.isOnline(activity) || GoogleBilling.isPremiumUser() || bannerCounter >= AdsManager.adData.BannerRequests)
            return

        loadAd(activity, adId, container, adSize, listener)
    }

    private fun loadAd(
        activity: Activity,
        adId: String,
        container: FrameLayout,
        size: BANNER = BANNER.NORMAL,
        listener: (Boolean) -> Unit = {}
    ) {

        val adView = AdView(activity)
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

        val adRequest = if (size == BANNER.NORMAL) {

            adView.setAdSize(AdSize.BANNER)
            AdRequest.Builder().build()

        } else {
            //collapseAbleBanner
            adView.setAdSize(getAdSize(activity, container))
            val extras = Bundle()

            if (size == BANNER.COLLAPSE_TOP)
                extras.putString("collapsible", "top")
            else
                extras.putString("collapsible", "bottom")


            extras.putString("collapsible_request_id", UUID.randomUUID().toString())

            AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                .build()
        }

        adView.loadAd(adRequest)

        bannerCounter += 1
    }

    private fun getAdSize(activity: Activity, bannerContainer: FrameLayout): AdSize {
        val display = activity.windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = bannerContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}