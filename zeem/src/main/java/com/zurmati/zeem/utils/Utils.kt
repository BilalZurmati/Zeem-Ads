package com.zurmati.zeem.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import com.zurmati.zeem.databinding.LoadingLayoutBinding

class Utils {

    companion object {
        private var loadingDialog: Dialog? = null

        fun showLoadingDialog(activity: Activity) {
            if (loadingDialog != null && loadingDialog?.isShowing == true)
                return

            loadingDialog = Dialog(activity)
            val inflater =
                activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val binding = LoadingLayoutBinding.inflate(inflater)

            loadingDialog?.setContentView(binding.root)
            loadingDialog?.setCancelable(false)

            val marginInDp = 55
            val marginInPixels = (marginInDp * activity.resources.displayMetrics.density).toInt()

            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            val width: Int = screenWidth - marginInPixels
            loadingDialog?.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)



            loadingDialog?.window?.decorView?.setBackgroundColor(Color.TRANSPARENT)
            loadingDialog?.show()
        }

        fun dismissLoadingDialog() {
            if (loadingDialog?.isShowing == true)
                loadingDialog?.dismiss()
        }

        fun isOnline(context: Context?): Boolean {
            if (context == null) return false
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val capabilities =
                    connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if (capabilities != null) {
                    when {
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            return true
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            return true
                        }

                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                            return true
                        }
                    }
                }
            } else {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            }
            return false
        }
    }
}