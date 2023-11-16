package com.zurmati.zeem.billing

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.preference.PowerPreference
import com.zurmati.zeem.ads.managers.AdsManager
import com.zurmati.zeem.databinding.PremiumDialogLayoutBinding
import com.zurmati.zeem.enums.Subscription
import com.zurmati.zeem.interfaces.PremiumVersionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class GoogleBilling {
    companion object {

        private const val Is_Premium = "Photo_Laps_Premium_User"
        private const val Is_Dialog_Shown = "Photo_Laps_Premium_User_Dialog_shown"

        private const val monthlySubscriptionName = "monthly_subs_id"
        private const val yearlySubscriptionName = "yearly_subscription_id"

        private var premiumVersionListener: PremiumVersionListener? = null

        private var isPremiumDialogShown = false

        /**
         * returns true if user has subscribed to any package
         */
        fun isPremiumUser(): Boolean =
            PowerPreference.getDefaultFile().getBoolean(Is_Premium, false)

        fun isPremiumDialogShown(): Boolean = isPremiumDialogShown

        private val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
                    CoroutineScope(Dispatchers.IO).launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }

                }
            }

        private suspend fun handlePurchase(purchase: Purchase?) {
            if (purchase?.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                    val ackPurchaseResult = withContext(Dispatchers.IO) {
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams.build()) {

                        }
                    }


                }

                premiumVersionListener?.onPremiumVersionActivated()
            }
        }

        /**
         * This method comes handy if user clear's app data and returns to the app
         */
        private suspend fun queryPreviousPurchases() {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)

            // uses queryPurchaseHistory Kotlin extension function
            val purchaseHistoryResult = billingClient?.queryPurchasesAsync(
                params.build()
            ) { p0, purchases ->

                if (purchases.isNotEmpty()) {
                    PowerPreference.getDefaultFile().putBoolean(Is_Premium, true)
                    premiumVersionListener?.onPremiumVersionActivated()
                } else {
                    PowerPreference.getDefaultFile().putBoolean(Is_Premium, false)
                    PowerPreference.getDefaultFile().putBoolean(Is_Dialog_Shown, false)
                    premiumVersionListener?.onPremiumVersionDeactivated()
                }

                Log.i("PurchasedHistory", "queryPreviousPurchases: ${purchases.size}")


            }


        }


        private var monthlyProduct: ProductDetails? = null
        private var yearlyProduct: ProductDetails? = null

        private var billingClient: BillingClient? = null

        /**
         * initialize Google Billing Client
         */
        fun init(context: Context) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(mBillingResult: BillingResult) {

                    val billingResult =
                        billingClient?.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)

                    if (mBillingResult.responseCode == BillingClient.BillingResponseCode.OK && billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {


                        CoroutineScope(Dispatchers.IO).launch {
                            queryPreviousPurchases()
                            fetchAvailableProducts()
                        }

                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })
        }


        fun getPrice(subscription: Subscription): String {
            return if (subscription == Subscription.MONTHLY)
                monthlyProduct?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                    0
                )?.formattedPrice ?: "N/A"
            else
                yearlyProduct?.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                    0
                )?.formattedPrice ?: "N/A"
        }

        private suspend fun fetchAvailableProducts() {
            val productList = ArrayList<QueryProductDetailsParams.Product>()
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(monthlySubscriptionName)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(yearlySubscriptionName)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
            val params = QueryProductDetailsParams.newBuilder()
            params.setProductList(productList)

            // leverage queryProductDetails Kotlin extension function
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient?.queryProductDetails(params.build())
            }

            // Process the result.
            productDetailsResult?.productDetailsList?.size
            monthlyProduct =
                productDetailsResult?.productDetailsList?.find { it.productId == monthlySubscriptionName }
            yearlyProduct =
                productDetailsResult?.productDetailsList?.find { it.productId == yearlySubscriptionName }

            Log.i("CurrentProductHere", "fetchAvailableProducts: $monthlyProduct")

        }


        private fun getProductDetails(subscription: Subscription): ProductDetails? {
            return if (subscription == Subscription.MONTHLY)
                monthlyProduct
            else
                yearlyProduct
        }


        /**
         * Product Details produces an exception for some devices that have old version of play store
         * I have added another method as well to handle this situation
         */
        fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails?) {

            productDetails?.let {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                        .setProductDetails(it)
                        // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                        // for a list of offers that are available to the user
                        .setOfferToken(it.subscriptionOfferDetails?.get(0)?.offerToken ?: "")
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient?.launchBillingFlow(activity, billingFlowParams)
            } ?: Toast.makeText(
                activity,
                "Unable to launch purchase flow for now",
                Toast.LENGTH_SHORT
            ).show()


        }


        /**
         * This listener needs to be register in all of your activities to show premium dialog without having leak window
         */
        fun setPremiumListener(listener: PremiumVersionListener?) {
            premiumVersionListener = listener
        }


        private fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
            return false
        }


        fun launchPurchaseFlow(activity: Activity, subscription: Subscription) {

            if (billingClient == null) {
                if (isNetworkConnected(activity) && billingClient == null) {
                    init(activity)
                }

                Toast.makeText(
                    activity,
                    "Unable to launch purchase flow for now, please check your internet connection",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }


            val billingResult =
                billingClient?.isFeatureSupported(BillingClient.FeatureType.PRODUCT_DETAILS)
            if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {

                Log.i("PremiumFlow", "launchPurchaseFlow: ${getProductDetails(subscription)}")

                getProductDetails(subscription)?.let {
                    val productDetailsParamsList = listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                            .setProductDetails(it)
                            // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                            // for a list of offers that are available to the user
                            .setOfferToken(it.subscriptionOfferDetails?.get(0)?.offerToken ?: "")
                            .build()
                    )

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    billingClient?.launchBillingFlow(activity, billingFlowParams)
                } ?: run {

                    Log.i("PremiumFlow", "launchPurchaseFlow: null product")

                    if (isNetworkConnected(activity) && billingClient == null) {
                        init(activity)
                    }

                    Toast.makeText(
                        activity,
                        "Unable to launch purchase flow for now, please check your internet connection",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                Toast.makeText(
                    activity,
                    "This feature is not supported for your device",
                    Toast.LENGTH_SHORT
                ).show()
            }


        }


        /**
         * included a default dialog which will be shown to user if he had a purchased previously
         * or subscribed to any product recently
         * you can change this logic according to your own need
         */
        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        fun showPremiumDialog(
            activity: Activity,
            restartIntent: Intent?,
            nextStepIntent: Intent? = null
        ) {
            Log.i("PurchasedHistory", "showPremiumDialog: called")
            if (PowerPreference.getDefaultFile().getBoolean(Is_Dialog_Shown, false))
                return



            Handler(Looper.getMainLooper()).post {
                val dialog = Dialog(activity)
                dialog.setCancelable(false)
                val inflater =
                    activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val binding = PremiumDialogLayoutBinding.inflate(inflater)
                dialog.setContentView(binding.root)


                binding.btnContinue.setOnClickListener {
                    dialog.dismiss()
                    AdsManager.clearInstances()
                    restartIntent?.let {
                        activity.finishAffinity()
                        activity.startActivity(it)
                    }

                    nextStepIntent?.let {
                        activity.startActivity(it)
                        activity.finish()
                    }

                }


                dialog.setOnDismissListener {
                    isPremiumDialogShown = false
                    PowerPreference.getDefaultFile().putBoolean(Is_Dialog_Shown, true)
                }


                isPremiumDialogShown = true


                val marginInDp = 45
                val marginInPixels =
                    (marginInDp * activity.resources.displayMetrics.density).toInt()

                val screenWidth = Resources.getSystem().displayMetrics.widthPixels
                val width: Int = screenWidth - marginInPixels
                dialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)

                dialog.show()
                dialog.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
                Log.i("PurchasedHistory", "showPremiumDialog: shown")


            }

        }


    }
}