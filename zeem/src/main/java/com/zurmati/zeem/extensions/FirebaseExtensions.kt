package com.zurmati.zeem.extensions

import android.os.Bundle
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

private val analytics by lazy {
    Firebase.analytics
}

fun logEvent(event: String) = analytics.logEvent(event, Bundle())