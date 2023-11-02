package com.zurmati.zeem.ads.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.MobileAds
import com.zurmati.zeem.enums.InterstitialDismiss
import com.zurmati.zeem.enums.Layout
import com.zurmati.zeem.interfaces.IAdEventListener
import com.zurmati.zeem.models.AdData

object AdsManager {
    private var admobManager = AdmobManager()
    private var cappingCounter = 0
    var adData = AdData()
    private var backInterstitialFlag = false
    private var landingInterstitialFlag = true

    fun initAdManager(activity: Activity, listener: IAdEventListener?) {

        MobileAds.initialize(activity.applicationContext) {
            admobManager.loadNativeAds(activity) {
                if (it)
                    listener?.onAdResponse()
            }

            prefetchingInterstitialAds(activity)
        }
    }

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
        Log.i("ZeemLogs", "$cappingCounter")
        return cappingCounter % adData.clickCapping == 0
    }

    fun countInterstitialCapping(context: Context) {
        admobManager.checkInterstitialInstances(context)
        if (cappingMatched())
            backInterstitialFlag = true
    }

    fun prefetchingInterstitialAds(context: Context) =
        admobManager.checkInterstitialInstances(context)

    fun showInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        Log.i("ZeemLogs", "showInterstitialAd")
        if (landingInterstitialFlag) {
            Log.i("ZeemLogs", "First Landing")
            if (admobManager.isInterstitialAdAvailable()) {
                admobManager.showInterstitialAd(activity, dismiss, listener)
                landingInterstitialFlag = false
            } else {
                Log.i("ZeemLogs", "else")
                listener.invoke(false)
                admobManager.checkInterstitialInstances(activity)
            }
        } else if (!cappingMatched()) {
            Log.i("ZeemLogs", "Does not matched")
            listener.invoke(false)
        } else if (admobManager.isInterstitialAdAvailable()) {
            Log.i("ZeemLogs", "interstitial available")
            admobManager.showInterstitialAd(activity, dismiss, listener)
        } else {
            Log.i("ZeemLogs", "none")
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