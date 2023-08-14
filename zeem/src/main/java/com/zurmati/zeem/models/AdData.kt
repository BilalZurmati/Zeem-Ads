package com.zurmati.zeem.models

data class AdData(
    var clickCapping: Int = 3,
    var nativeRequest: Int = 1,
    var interstitialRequests: Int = 1,
    var BannerRequests: Int = 1,
    var nativeId: String = "ca-app-pub-3940256099942544/2247696110",
    var interstitialId: String = "",
    var bannerId: String = ""

)
