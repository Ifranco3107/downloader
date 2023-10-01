package com.macropay.downloader.utils.phone

import android.content.Context
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.data.logs.Log.w
import android.media.ToneGenerator
import android.media.AudioManager
import java.lang.RuntimeException

class ToneDTMF(private val context: Context) {
    private var mToneGenerator: ToneGenerator? = null
    private val mToneGeneratorLock = Any()
    private val TAG = "ToneDTMF"

    init {
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    msg(TAG, "Inicializar el mToneGenerator")
                    mToneGenerator = ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME)
                } catch (e: RuntimeException) {
                    //    Log.w(LOG_TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null
                    guardar(TAG, "constructor", e.message)
                }
            }
        }
    }

    fun playTone(tone: Int) {
        // if local tone playback is disabled, just return.
        /* if (!mDTMFToneEnabled) {
            return;
        }*/

        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val ringerMode = audioManager.ringerMode
        if (ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
            msg(TAG, "Se salio - ringerMode: $ringerMode")
            return
        }
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator == null) {
                w(TAG, "playTone: mToneGenerator == null, tone: $tone")
                return
            }
            w(TAG, "startTone")
            // Start the new tone (will stop any playing tone)
            mToneGenerator!!.startTone(tone, TONE_LENGTH_MS)
        }
    }

    fun destroy() {
        synchronized(mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator!!.release()
                mToneGenerator = null
            }
        }
    }

    companion object {
        /** The length of DTMF tones in milliseconds  */
        private const val TONE_LENGTH_MS = 150

        /** The DTMF tone volume relative to other sounds in the stream  */
        private const val TONE_RELATIVE_VOLUME = 80

        /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys  */
        private const val DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF
        private const val BAD_EMERGENCY_NUMBER_DIALOG = 0
    }
}