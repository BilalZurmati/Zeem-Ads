package com.zurmati.zeem.ads.admob

import android.app.Activity
import android.content.Context
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentForm
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

class UMP {
    companion object {

        // Create a ConsentRequestParameters object.
        private val params: ConsentRequestParameters = ConsentRequestParameters
            .Builder()
            .build()

//        ConsentDebugSettings.Builder(activity)
//        .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//        .addTestDeviceHashedId("3EF6849B8717BDC8545B055DA1C933EA")
//        .build()

        /**
         * This method will show consent dialog to the user.
         */
        fun checkConsent(activity: Activity) {

            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            consentInformation.requestConsentInfoUpdate(activity, params, {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    // Consent has been gathered.
                }
            },
                { requestConsentError ->
                    // Consent gathering failed.

                })
        }
    }
}