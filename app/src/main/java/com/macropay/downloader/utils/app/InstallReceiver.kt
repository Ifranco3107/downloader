package com.macropay.downloader.utils.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.media.AudioManager
import android.media.ToneGenerator
import com.macropay.downloader.receivers.HiltBroadcasterReceiver
import com.macropay.utils.logs.Log
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "InstallReceiver"
@AndroidEntryPoint
class InstallReceiver : HiltBroadcasterReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.msg(TAG, "received ${intent.action}  ")
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val activityIntent =
                    intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                Log.msg(TAG, "received STATUS_PENDING_USER_ACTION ")
                context.startActivity(activityIntent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
            PackageInstaller.STATUS_SUCCESS ->{

                ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    .startTone(ToneGenerator.TONE_PROP_ACK)
                Log.msg(TAG, "received STATUS_SUCCESS ")
            }

            else -> {
                val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

                Log.e(TAG, "received $status and $msg")
            }
        }
    }
}