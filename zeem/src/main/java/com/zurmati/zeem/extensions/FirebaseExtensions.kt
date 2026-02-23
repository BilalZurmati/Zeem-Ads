package com.zurmati.zeem.extensions

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

private val analytics by lazy {
    Firebase.analytics
}

fun logEvent(event: String) = analytics.logEvent(event, Bundle())