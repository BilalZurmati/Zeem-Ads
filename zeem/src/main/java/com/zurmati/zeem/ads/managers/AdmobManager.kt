package com.zurmati.zeem.ads.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.AdSize
import com.zurmati.zeem.ads.admob.AdmobBannerAd
import com.zurmati.zeem.ads.admob.AdmobInterstitialAd
import com.zurmati.zeem.ads.admob.AdmobNativeAd
import com.zurmati.zeem.enums.InterstitialDismiss
import com.zurmati.zeem.enums.Layout

class AdmobManager {

    private var nativeAd1: AdmobNativeAd = AdmobNativeAd()
    private var nativeAd2: AdmobNativeAd = AdmobNativeAd()

    fun isNativeAdAvailable(): Boolean = nativeAd1.isAdAvailable() || nativeAd2.isAdAvailable()

    fun loadNativeAds(context: Context, listener: (Boolean) -> Unit = {}) {
        nativeAd1.loadFreshNative(context, AdsManager.adData.nativeId, listener)
    }

    fun showNativeAd(
        context: Context,
        container: FrameLayout,
        layout: Layout,
        inflated: (Boolean) -> Unit = {}
    ) {
        if (nativeAd1.isAdAvailable()) {
            Log.i("MyNativeAdResponse", "showNativeAd: nativeAd1")

            nativeAd1.showAd(context, container, layout, inflated)
        } else if (nativeAd2.isAdAvailable()) {
            Log.i("MyNativeAdResponse", "showNativeAd: nativeAd2")

            nativeAd2.showAd(context, container, layout, inflated)

            nativeAd1.loadFreshNative(context, AdsManager.adData.nativeId)
        } else {
            Log.i("MyNativeAdResponse", "showNativeAd: else")

            inflated.invoke(false)
            nativeAd1.loadFreshNative(context, AdsManager.adData.nativeId)
            nativeAd2.loadFreshNative(context, AdsManager.adData.nativeId)
        }
    }

    fun checkNativeInstances(context: Context) {
        if (!nativeAd1.isAdAvailable())
            nativeAd1.loadFreshNative(context, AdsManager.adData.nativeId)

        if (!nativeAd2.isAdAvailable())
            nativeAd2.loadFreshNative(context, AdsManager.adData.nativeId)
    }


    private var interstitialAd1: AdmobInterstitialAd = AdmobInterstitialAd()

    fun isInterstitialAdAvailable(): Boolean = interstitialAd1.isAdAvailable()

    fun isInterstitialShowing(): Boolean = interstitialAd1.interstitialShowing

    fun showInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        if (interstitialAd1.isAdAvailable()) {
            interstitialAd1.showInterstitial(activity, dismiss, listener)
        }  else {
            interstitialAd1.loadFreshInterstitial(activity, AdsManager.adData.interstitialId)
            listener.invoke(false)
        }
    }


    fun checkInterstitialInstances(context: Context) {
        if (!interstitialAd1.isAdAvailable())
            interstitialAd1.loadFreshInterstitial(context, AdsManager.adData.interstitialId)
    }


    private var bannerAd: AdmobBannerAd = AdmobBannerAd()

    fun loadBannerAd(
        activity: Activity,
        container: FrameLayout,
        bannerSize: AdSize = AdSize.BANNER,
        listener: (Boolean) -> Unit = {}
    ) {
        bannerAd.loadFreshBanner(
            activity,
            AdsManager.adData.bannerId,
            container,
            bannerSize,
            listener
        )
    }

}