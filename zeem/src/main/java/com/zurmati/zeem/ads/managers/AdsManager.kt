package com.zurmati.zeem.ads.managers

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.zurmati.zeem.enums.InterstitialDismiss
import com.zurmati.zeem.enums.Layout
import com.zurmati.zeem.models.AdData

object AdsManager {
    private var admobManager = AdmobManager()
    private var cappingCounter = 0
    var adData = AdData()
    private var backInterstitialFlag = false


    fun showNativeAd(
        context: Context,
        container: FrameLayout,
        layout: Layout,
        inflated: (Boolean) -> Unit = {}
    ) {
        if (admobManager.isNativeAdAvailable()) {
            admobManager.showNativeAd(context, container, layout, inflated)
        } else {
            admobManager.checkNativeInstances(context)
        }
    }


    private fun cappingMatched(): Boolean {
        cappingCounter += 1
        return adData.clickCapping % cappingCounter == 0
    }

    fun countInterstitialCapping(context: Context) {
        admobManager.checkInterstitialInstances(context)
        if (cappingMatched())
            backInterstitialFlag = true
    }

    fun showInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        if (!cappingMatched()) {
            listener.invoke(false)
        } else if (admobManager.isInterstitialAdAvailable()) {
            admobManager.showInterstitialAd(activity, dismiss, listener)
        } else {
            listener.invoke(false)
            admobManager.checkInterstitialInstances(activity)
        }
    }


    fun showInterstitialAdOnBack(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        if (!backInterstitialFlag) {
            listener.invoke(false)
        } else if (admobManager.isInterstitialAdAvailable()) {
            backInterstitialFlag = false
            admobManager.showInterstitialAd(activity, dismiss, listener)
        } else {
            listener.invoke(false)
            admobManager.checkInterstitialInstances(activity)
        }
    }


    fun loadBannerAd(context: Context, container: FrameLayout, listener: (Boolean) -> Unit = {}) {
        admobManager.loadBannerAd(context, container, listener)
    }

    fun clearInstances() {
        admobManager = AdmobManager() // Create a new instance of AdmobManager
        cappingCounter = 0 // Reset the capping counter
        adData = AdData() // Create a new instance of AdData
        backInterstitialFlag = false //Reset the backInterstitialFlag
    }

}