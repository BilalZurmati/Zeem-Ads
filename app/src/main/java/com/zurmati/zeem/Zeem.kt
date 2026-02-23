package com.zurmati.zeem

import android.app.Application
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling

class Zeem : Application() {
    override fun onCreate() {
        super.onCreate()
        AdsManager.initAppOpen(this, "Google_Billing_id")
    }
}