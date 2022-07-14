package com.nayan.strangers.models

import android.webkit.JavascriptInterface
import com.nayan.strangers.activites.CallActivity

class InterfaceKotlin(callActivity: CallActivity) {
    private lateinit var callActivity: CallActivity

    init {
        this.callActivity = CallActivity()
    }

    @JavascriptInterface
    public fun onPeerConnected(){
        callActivity.onPeerConneted()
    }
}