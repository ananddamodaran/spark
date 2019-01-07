package com.viginfotech.spark.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber

class FCMNotification: FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        Timber.d("message received ${remoteMessage.toString()}")

    }
}