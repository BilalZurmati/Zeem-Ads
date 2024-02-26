package com.zurmati.zeem.ads.admob

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.zurmati.zeem.R
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.enums.Layout
import com.zurmati.zeem.extensions.logEvent

class AdmobNativeAd {

    private var nativeAd: NativeAd? = null
    private var nativeLoading: Boolean = false
    private var nativeCounter = 0


    private var adRequest = AdRequest.Builder().build()


    fun loadFreshNative(context: Context, adId: String, listener: (Boolean) -> Unit = {}) {
        if (GoogleBilling.isPremiumUser() || nativeLoading || nativeCounter >= AdsManager.adData.nativeRequest)
            return

        loadAd(context, adId, listener)
    }

    private fun loadAd(context: Context, adId: String, listener: (Boolean) -> Unit = {}) {
        Log.i("PhotoTranslatorLogs", "AdmobNativeAd request")

        val adLoader = AdLoader.Builder(context, adId)
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad

            }
            .withAdListener(object : AdListener() {

                override fun onAdClicked() {
                    super.onAdClicked()
                    logEvent("NativeAdClicked")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    logEvent("NativeAdImpression")
                    nativeAd = null
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    listener.invoke(false)
                    nativeLoading = false
                    Log.i("PhotoTranslatorLogs", "AdmobNativeAd Error $adError")
                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    nativeLoading = false
                    listener.invoke(true)
                    AdsManager.getNativeLoadListener()?.onAdResponse()
                    Log.i("PhotoTranslatorLogs", "AdmobNativeAd Loaded")

                }
            }).build()

        adLoader.loadAd(adRequest)
        nativeCounter += 1
        nativeLoading = true
    }


    fun isAdAvailable(): Boolean = nativeAd != null

    fun showAd(
        context: Context,
        container: FrameLayout,
        layout: Layout,
        inflated: (Boolean) -> Unit = {}
    ) {
        val adView = when (layout) {
            Layout.FULL -> {
                LayoutInflater.from(context).inflate(R.layout.admob_native_full, null)
            }

            Layout.SIDE_MEDIA -> {
                LayoutInflater.from(context).inflate(R.layout.admob_native_side_media, null)
            }

            Layout.NO_MEDIA -> {
                LayoutInflater.from(context).inflate(R.layout.admob_native_no_media, null)
            }

            Layout.SIDE_ICON -> {
                LayoutInflater.from(context).inflate(R.layout.admob_native_side_icon, null)
            }

            Layout.NO_ICON -> {
                LayoutInflater.from(context).inflate(R.layout.admob_native_no_icon, null)
            }


        } as NativeAdView



        when (layout) {
            Layout.FULL -> {
                inflateFull(adView, nativeAd)
            }

            Layout.SIDE_MEDIA -> {
                inflateSideMedia(adView, nativeAd)
            }

            Layout.NO_MEDIA -> {
                inflateNoMedia(adView, nativeAd)
            }

            Layout.SIDE_ICON -> {
                inflateSideIcon(adView, nativeAd)
            }

            Layout.NO_ICON -> {
                inflateNoIcon(adView, nativeAd)
            }


        }

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)


        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd?.headline


        nativeAd?.callToAction?.let {
            (adView.callToActionView as Button).text = it
        }


        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd!!)

//        adView.parent?.let {
//            (it as ViewGroup).removeAllViews()
//        }

        container.removeAllViews()
        container.addView(adView)

        if (container.visibility != View.VISIBLE)
            container.visibility = View.VISIBLE


        inflated.invoke(true)

    }

    private fun inflateNoIcon(adView: NativeAdView, nativeAd: NativeAd?) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)

        adView.bodyView = adView.findViewById(R.id.ad_body)


        adView.mediaView?.let {
            it.mediaContent = nativeAd!!.mediaContent
        }

        nativeAd?.body?.let {
            (adView.bodyView as TextView).text = it
        }
    }
    private fun inflateSideIcon(adView: NativeAdView, nativeAd: NativeAd?) {
        // Set the media view.
        adView.iconView = adView.findViewById(R.id.ad_app_icon)

        nativeAd?.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(
                it.drawable
            )
        }
    }

    private fun inflateNoMedia(adView: NativeAdView, nativeAd: NativeAd?) {
        // Set the media view.
        adView.iconView = adView.findViewById(R.id.ad_app_icon)

        nativeAd?.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(
                it.drawable
            )
        }
    }

    private fun inflateSideMedia(adView: NativeAdView, nativeAd: NativeAd?) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.bodyView = adView.findViewById(R.id.ad_body)


        adView.mediaView?.let {
            it.mediaContent = nativeAd!!.mediaContent
        }

        nativeAd?.icon?.let {
            (adView.iconView as ImageView).setImageDrawable(
                it.drawable
            )
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        nativeAd?.body?.let {
            (adView.bodyView as TextView).text = it
        }

    }

    private fun inflateFull(adView: NativeAdView, nativeAd: NativeAd?) {
        // Set the media view.
        adView.mediaView = adView.findViewById(R.id.ad_media)


        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)


        adView.mediaView?.let {
            it.mediaContent = nativeAd!!.mediaContent
        }


        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        nativeAd?.body?.let {
            (adView.bodyView as TextView).text = it
        }

        nativeAd?.price?.let {
            (adView.priceView as TextView).text = it
        } ?: {
            adView.priceView!!.visibility = View.INVISIBLE
        }

        nativeAd?.store?.let {
            (adView.storeView as TextView).text = it
        } ?: {
            adView.storeView!!.visibility = View.INVISIBLE
        }

        nativeAd?.starRating?.let {
            (adView.starRatingView as RatingBar).rating = it.toFloat()
        }

        nativeAd?.advertiser.let {
            (adView.advertiserView as TextView).text = it
        }
    }


}