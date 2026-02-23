package com.zurmati.zeem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.billing.GoogleBilling
import com.zurmati.zeem.interfaces.IAdEventListener
import com.zurmati.zeem.interfaces.PremiumVersionListener

class MainActivity : AppCompatActivity(), IAdEventListener, PremiumVersionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AdsManager.initAdManager(this, this)
        GoogleBilling.init(this)


        GoogleBilling.setPremiumListener(this)


    }

    /**
     * This method returns the callback when the native ad is successfully loaded and ready to show
     */
    override fun onAdResponse() {

    }

    /**
     * If user buys a package then we show a dialog with the success message and upon dismissing that dialog we
     * restart the app in order to clear all the pre-loaded ads.
     */
    override fun onPremiumVersionActivated() {
        if (!this.isFinishing && this.window.decorView.isVisible) {
            GoogleBilling.showPremiumDialog(
                this@MainActivity,
                null,
                Intent(this@MainActivity, MainActivity::class.java)
            )
        }
    }

    override fun onPremiumVersionDeactivated() {
        TODO("Not yet implemented")
    }
}