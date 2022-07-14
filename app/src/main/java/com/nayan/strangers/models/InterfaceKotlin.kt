package com.nayan.strangers.models

import android.webkit.JavascriptInterface
import com.nayan.strangers.activites.CallActivity

class InterfaceKotlin(var callActivity: CallActivity) {

    @JavascriptInterface
    fun onPeerConnected(){
        callActivity.onPeerConnected()
    }
}