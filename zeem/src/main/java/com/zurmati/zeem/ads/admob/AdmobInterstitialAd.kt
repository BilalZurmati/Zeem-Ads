package com.zurmati.zeem.ads.admob

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.enums.InterstitialDismiss
import com.zurmati.zeem.extensions.logEvent

class AdmobInterstitialAd {
    private var mInterstitialAd: InterstitialAd? = null
    private var interstitialAdLoading: Boolean = false
    private var interstitialCounter = 0

    private var adRequest = AdRequest.Builder().build()


    fun loadFreshInterstitial(context: Context, adId: String, listener: (Boolean) -> Unit = {}) {
        if (GoogleBilling.isPremiumUser() || interstitialAdLoading || interstitialCounter >= AdsManager.adData.interstitialRequests)
            return

        loadAd(context, adId, listener)
    }

    private fun loadAd(context: Context, adId: String, listener: (Boolean) -> Unit = {}) {

        InterstitialAd.load(context, adId, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    interstitialAdLoading = false
                    mInterstitialAd = null
                    listener.invoke(false)
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAdLoading = false
                    mInterstitialAd = interstitialAd
                    listener.invoke(true)
                }
            })
        interstitialCounter += 1
        interstitialAdLoading = true
    }


    fun isAdAvailable(): Boolean = mInterstitialAd != null

    fun showInterstitial(
        activity: Activity,
        dismissType: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                logEvent("InterstitialAdClicked")
            }

            override fun onAdDismissedFullScreenContent() {
                mInterstitialAd = null
                if (dismissType == InterstitialDismiss.ON_CLOSE)
                    listener.invoke(true)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                super.onAdFailedToShowFullScreenContent(p0)
                listener.invoke(false)
            }

            override fun onAdImpression() {
                if (dismissType == InterstitialDismiss.ON_IMPRESSION)
                    listener.invoke(true)

                logEvent("InterstitialAdImpression")

            }

            override fun onAdShowedFullScreenContent() {
            }
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(activity)
        }
    }
}