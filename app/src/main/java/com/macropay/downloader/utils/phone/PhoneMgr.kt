package com.macropay.downloader.utils.phone

import android.Manifest
import com.macropay.downloader.utils.Settings.getSetting
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.downloader.ui.common.mensajes.ToastDPC.showToast
import android.app.Activity
import android.content.pm.PackageManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.macropay.downloader.data.preferences.TipoBloqueo
import android.os.UserManager
import android.os.Bundle
import android.os.Handler
import android.telecom.TelecomManager
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.macropay.downloader.DeviceAdminReceiver
import java.lang.Exception

class PhoneMgr(context: Context?) : Activity() {
    var TAG = "PhoneMgr"
    var mContext: Context? = null

    init {
        println("PhoneMgr - CONSTRUCTOR")
        if (context == null) println("PhoneMgr - context == null")
        mContext = context
        attachBaseContext(context)
    }

    //Revisa permisos para hacer llamadas, y hace la llamada.
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_PHONE_STATE),
                    42
                )
            }
        } else {
            // Permission has already been granted
            // callPhone();
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            42 -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    // permission was granted, yay!
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }

    /*

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == 42) {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay!
                    callPhone()
                } else {
                    // permission denied, boo! Disable the
                    // functionality
                }
                return
            }
        }*/
    fun disableCall(context: Context?, bActiva: Boolean) {
        var ln = 0
        try {
            ln = 1
            val mDevicePolicyManager: DevicePolicyManager
            ln = 2
            val mAdminComponentName: ComponentName
            ln = 3
            mDevicePolicyManager = context!!.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
            ln = 4
            mAdminComponentName = DeviceAdminReceiver.getComponentName(this)
            val disableIncoming = getSetting(TipoBloqueo.disable_incoming_calls, false)
            val disableOutgoing = getSetting(TipoBloqueo.disable_outgoing_calls, false)
            msg(TAG, "[disableCall] bActiva: $bActiva")
            msg(TAG, "[disableCall] disableIncoming: $disableIncoming")
            msg(TAG, "[disableCall] disableOutgoing: $disableOutgoing")
            ln = 5
            if (bActiva) {
                ln = 6
                mDevicePolicyManager.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_OUTGOING_CALLS)
            } else {
                ln = 7
                mDevicePolicyManager.clearUserRestriction(mAdminComponentName, UserManager.DISALLOW_OUTGOING_CALLS)
            }
            ln = 8
        } catch (ex: Exception) {
            msg("[disableCall]", "ERROR[" + ln + "]" + ex.message)
        }
    }

    fun callPhone(telefono: String) {
        try {
            //telefono +=",,,,,,1";
            // RECOMENDABLE HACER LLAMADAS CON EL SERVICIO NATIVO.
            //https://developer.android.com/reference/android/telecom/ConnectionService

            //NOTA; no se debe usar la siquiente linea, se usa bloqueo manual.
            //  disableCall(mContext,false);
            msg(TAG, "callPhone: $telefono")
            checkPermission()

            //  val telefono: String = Config.telefono()
            if (telefono != "") {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$telefono"))
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
                //intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                mContext!!.startActivity(intent)
            }
        } catch (ex: Exception) {
            guardar(TAG, "PhoneMgr", ex.message)
        }
    }

    fun placeCall(telefono: String) {
        //Esquema 1  CallManager.call(this,"8006276729");
        disableCall(mContext, false)
        //esquema 2
        //Uri.parse("tel://8006276729,1");
        //DTMF Tones+  ",,,,,,1"
        //8006276729,,,,1  -Cada coma es un segundo de delay. en este caso 4 segundos
        try {
            val uri = Uri.fromParts("tel", telefono, "1")
            msg(TAG, "placeCall: $telefono")
            val extras = Bundle()
            extras.putBoolean(TelecomManager.EXTRA_OUTGOING_CALL_EXTRAS, true)
            extras.putBoolean(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, true)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val telecomManager = this.getSystemService(TELECOM_SERVICE) as TelecomManager
            //  telecomManager.getDefaultDialerPackage();
            //  telecomManager.registerPhoneAccount(phoneAccount);
            telecomManager.showInCallScreen(true)
            // CallManager.registerCallback(new Callback(binding));
            telecomManager.placeCall(uri, extras)
        } catch (ex: Exception) {
            guardar(TAG, "PhoneMgr", ex.message)
        }
    }

    fun sendDTMF(view: View?) {
        msg(TAG, "sendDTMF")
        try {
            val handler = Handler(Looper.getMainLooper())
            handler.post { showToast(this, "1") }
            msg(TAG, "va ejecutar el toneDTMF")
            val toneDTMF = ToneDTMF(this)
            toneDTMF.playTone(2)
        } catch (ex: Exception) {
            guardar(TAG, "sendDTMF", ex.message)
        }
    }

    private fun killCall() {
        msg(TAG, "*************************** Entro a KillCall... ************************")
        val tm = mContext!!.getSystemService(TELECOM_SERVICE) as TelecomManager
        if (tm != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            val success = tm.endCall()
            // success == true if call was terminated.
        }
    }
}