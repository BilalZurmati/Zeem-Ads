package com.zurmati.zeem.ads.managers

import android.app.Activity
import android.content.Context
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
            nativeAd1.showNative(context, container, layout, inflated)
        } else if (nativeAd2.isAdAvailable()) {
            nativeAd2.showNative(context, container, layout, inflated)

            nativeAd1.loadFreshNative(context, AdsManager.adData.nativeId)
        } else {
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
    private var interstitialAd2: AdmobInterstitialAd = AdmobInterstitialAd()

    fun isInterstitialAdAvailable(): Boolean =
        interstitialAd1.isAdAvailable() || interstitialAd2.isAdAvailable()

    fun isInterstitialShowing(): Boolean =
        interstitialAd1.interstitialShowing || interstitialAd2.interstitialShowing

    fun showInterstitialAd(
        activity: Activity,
        dismiss: InterstitialDismiss,
        listener: (Boolean) -> Unit = {}
    ) {
        if (interstitialAd1.isAdAvailable()) {
            interstitialAd1.showInterstitial(activity, dismiss, listener)
        } else if (interstitialAd2.isAdAvailable()) {
            interstitialAd2.showInterstitial(activity, dismiss, listener)

            interstitialAd1.loadFreshInterstitial(activity, AdsManager.adData.interstitialId)
        } else {
            listener.invoke(false)
            interstitialAd1.loadFreshInterstitial(activity, AdsManager.adData.interstitialId)
            interstitialAd2.loadFreshInterstitial(activity, AdsManager.adData.interstitialId)
        }
    }


    fun checkInterstitialInstances(context: Context) {
        if (!interstitialAd1.isAdAvailable())
            interstitialAd1.loadFreshInterstitial(context, AdsManager.adData.interstitialId)

        if (!interstitialAd2.isAdAvailable())
            interstitialAd2.loadFreshInterstitial(context, AdsManager.adData.interstitialId)
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