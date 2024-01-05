package com.zurmati.zeem.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
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
    }
}