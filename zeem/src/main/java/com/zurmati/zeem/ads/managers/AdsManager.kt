package com.zurmati.zeem.ads.managers

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.zurmati.zeem.ads.admob.AdmobAppOpen
import com.zurmati.zeem.ads.admob.UMP
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.enums.BANNER
import com.zurmati.zeem.enums.InterstitialDismiss
import com.zurmati.zeem.enums.Layout
import com.zurmati.zeem.interfaces.IAdEventListener
import com.zurmati.zeem.interfaces.IResumeListener
import com.zurmati.zeem.models.AdData

object AdsManager {
    private var admobManager = AdmobManager()
    private var cappingCounter = 0
    var adData = AdData()
    private var backInterstitialFlag = false
    private var landingInterstitialFlag = true

    private var sdkInitialized = false
    var resumeListener: IResumeListener? = null

    var appOpenShownNow = false

    private var nativeListener: IAdEventListener? = null


    /**
     * Make sure to call this method on Dashboard so that we collect user consent.
     */
    fun checkConsent(activity: Activity) = UMP.checkConsent(activity)


    /**
     * Call this method in your apps entry point ( Splash Activity )
     */
    fun initAdManager(context: Context, listener: IAdEventListener?) {
        if (GoogleBilling.isPremiumUser() || sdkInitialized)
            return

        MobileAds.initialize(context.applicationContext) {
            sdkInitialized = true
            admobManager.loadNativeAds(context) {
                if (it)
                    listener?.onAdResponse()
            }

            prefetchingInterstitialAds(context)
        }
    }


    /**
     * If you want real time native ad then make sure to register this listener
     * This listener will return a method who is going to be called every time a new native AD is loaded.
     */
    fun setNativeLoadListener(listener: IAdEventListener) {
        nativeListener = listener
    }

    fun getNativeLoadListener(): IAdEventListener? = nativeListener

    /**
     * Call this method wherever you want to inflate a native ad
     */
    fun showNativeAd(
        context: Context,
        container: FrameLayout,
        layout: Layout,
        inflated: (Boolean) -> Unit = {}
    ) {
        if (admobManager.isNativeAdAvailable()) {
            admobManager.showNativeAd(context, container, layout, inflated)
        } else {
            inflated.invoke(false)
            admobManager.checkNativeInstances(context)
        }
    }


    private fun cappingMatched(context: Context): Boolean {
        cappingCounter += 1
        Log.i("ZeemLogs", "$cappingCounter")
        if ((cappingCounter + 1) % adData.clickCapping == 0)
            admobManager.checkInterstitialInstances(context)

        return cappingCounter % adData.clickCapping == 0
    }

    fun countInterstitialCapping(context: Context) {
        if (appOpenShownNow) {
            appOpenShownNow = false
            return
        }

        if (cappingMatched(context))
            backInterstitialFlag = true
    }

    fun prefetchingInterstitialAds(context: Context) =
        admobManager.checkInterstitialInstances(context)

    fun isInterstitialShowing(): Boolean = admobManager.isInterstitialShowing()

    fun showInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        Log.i("ZeemLogs", "showInterstitialAd")
        if (appOpenShownNow) {
            appOpenShownNow = false
            listener.invoke(false)
            return
        }

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
        } else if (!cappingMatched(activity)) {
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

    fun showLandingInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        if (appOpenShownNow) {
            appOpenShownNow = false
            listener.invoke(false)
            return
        }

        if (!landingInterstitialFlag) {
            Log.i("ZeemLogs", "First Landing")
            listener.invoke(false)
        } else if (admobManager.isInterstitialAdAvailable()) {
            Log.i("ZeemLogs", "interstitial available")
            admobManager.showInterstitialAd(activity, dismiss, listener)
            landingInterstitialFlag = false
        } else {
            Log.i("ZeemLogs", "none")
            listener.invoke(false)
            admobManager.checkInterstitialInstances(activity)
        }
    }


    fun showInterstitialAdOnBack(
        activity: Activity,
        dismiss: InterstitialDismiss = InterstitialDismiss.ON_CLOSE,
        listener: (Boolean) -> Unit = {}
    ) {

        //in order to avoid app open and interstitial AD one after other, we need to ignore that condition
        if (appOpenShownNow) {
            appOpenShownNow = false
            listener.invoke(false)
            return
        }

        if (!backInterstitialFlag) {
            listener.invoke(false)
        } else if (admobManager.isInterstitialAdAvailable()) {

            Handler(Looper.getMainLooper()).postDelayed({
                backInterstitialFlag = false
                admobManager.showInterstitialAd(activity, dismiss, listener)
            }, 400)

        } else {
            listener.invoke(false)
            admobManager.checkInterstitialInstances(activity)
        }
    }

    fun checkIntegration(context: Context) {
        MobileAds.openAdInspector(context) { error ->
            // Error will be non-null if ad inspector closed due to an error.
        }
    }


    fun loadBannerAd(
        activity: Activity,
        container: FrameLayout,
        banner: BANNER = BANNER.NORMAL,
        listener: (Boolean) -> Unit = {}
    ) {
        admobManager.loadBannerAd(activity, container, banner, listener)
    }

    fun initAppOpen(application: Application, appOpenId: String) {
        val appOpen = AdmobAppOpen()
        appOpen.init(application, appOpenId)
    }

    fun clearInstances() {
        admobManager = AdmobManager() // Create a new instance of AdmobManager
        cappingCounter = 0 // Reset the capping counter
        adData = AdData() // Create a new instance of AdData
        backInterstitialFlag = false //Reset the backInterstitialFlag
        sdkInitialized = false
        appOpenShownNow = false
    }

}