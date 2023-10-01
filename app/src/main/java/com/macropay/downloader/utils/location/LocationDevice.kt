package com.macropay.downloader.utils.location

import android.Manifest
import com.macropay.downloader.utils.Settings.getSetting
import com.macropay.downloader.utils.activities.Dialogs.showActivity
import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnCompleteListener
import com.macropay.downloader.data.preferences.TipoParametro
import android.os.Build
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.macropay.downloader.ui.provisioning.EnrollActivity
import com.macropay.utils.preferences.Cons
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.macropay.downloader.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.downloader.data.preferences.TipoBloqueo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.Exception
import javax.inject.Inject

@SuppressLint("MissingPermission")
class LocationDevice
@Inject constructor(
    @ApplicationContext var context:Context,
    private val locationSender: LocationSender) {

    private val TAG = "LocationDevice"
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var mContext: Context? = null

    init {
    //    Log.msg(TAG, "<+>+<+>+<+>+<+> LocationDevice - Constructor <+>+<+>+<+>+<+>")
        try {
            mContext = context
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "LocationDevice", ex.message)
        }
    }

    private val cancellationTokenSource = CancellationTokenSource()
    var locationRequest: com.google.android.gms.location.LocationRequest? = null // com.google.android.gms.location.LocationRequest.create();
    private fun locationListener(): OnSuccessListener<Location> {
        return OnSuccessListener { location ->
            try {
                Log.msg(TAG, "onSuccess")
                if (location != null) {
                    locationSender.sendPos(location, mContext,TAG) //locationListener
                } else Log.msg(TAG, "location = null")
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "OnSuccessListener", ex.message)
            }
        }
    }

    private fun locationListenerComplete(): OnCompleteListener<Location> {
        return OnCompleteListener { task ->
            Log.msg(TAG, "onComplete")
            try {
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    locationSender.sendPos(location, mContext,TAG) //OnCompleteListener
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "onComplete", ex.message)
            }
        }
    }//  getPermisions();

    //Esta validacion, se puse para evitar que requiera GPS con mucha frecuencia, y el SO envie mensaje de consumo excesivo de bateria.
    // Metodo para hacer peticion OnDemand.
    @get:SuppressLint("MissingPermission")
    val currentPos: Boolean
        get() {
            //  getPermisions();
            //     Log.msg(TAG,"getCurrentPos");
            val enableTracking = getSetting(TipoBloqueo.disable_tracking_GPS, false)
            if (!enableTracking) return false
            //Esta validacion, se puse para evitar que requiera GPS con mucha frecuencia, y el SO envie mensaje de consumo excesivo de bateria.
            val fecEnvioGPS = getSetting(TipoParametro.frecuenciaCapturaGPS, 10)
            if (!locationSender.vencioLimiteSinEnvio(fecEnvioGPS)) {
                Log.msg(TAG, "[currentPos] Tiene menos de $fecEnvioGPS minutos de haber requerido GPS, SE SALE... ")
                return false
            }

            if (Utils.SDK_INT >= Build.VERSION_CODES.Q) {
                requireLocation()
            } else {
                createLocationRequest_P(mContext, "LocationDevice.getCurrentPos")
            }
            return true
        }

    //Para Android P -28
    fun createLocationRequest_P(context: Context?, source: String) {
        Log.msg(TAG, "[createLocationRequest_P] source: [$source]")
        try {
/*            if(locationRequest  != null) {
                Log.msg(TAG,"createLocationRequest_P - NO CREO EL Request.  SE SALE");
                return;
            }*/
            locationRequest = com.google.android.gms.location.LocationRequest.create()
            locationRequest!!.interval = 300000 //CADA 5 minutos
            locationRequest!!.fastestInterval = 60000 //Cada minuto
            //   locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest!!.priority = com.google.android.gms.location.LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            //     locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_LOW_POWER);
            //  locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_NO_POWER);
            Log.msg(TAG, "[createLocationRequest_P] - 1 -")
            val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest!!)
            val client = LocationServices.getSettingsClient(context!!)
            val task = client.checkLocationSettings(builder.build())
            Log.msg(TAG, "[createLocationRequest_P] - 2 -")
            task.addOnSuccessListener {
                Log.msg(TAG, "[onSuccess]")
                var response: LocationSettingsResponse? = null
                try {
                    response = task.getResult(ApiException::class.java)
                    //   Log.msg(TAG, "[onSuccess] - response: " + response.toString());
                    if (response.locationSettingsStates!!.isLocationPresent) {
                        Log.msg(TAG, "[onSuccess] - isLocationPresent= true ")
                        // getCurrentPos();
                        requireLocation()
                    }
                } catch (e: ApiException) {
                    ErrorMgr.guardar(TAG, "onSuccess", e.message)
                }
            }
            task.addOnFailureListener { e ->
                Log.msg(TAG, "onFailure: " + e.message)
                try {
                    if (e is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            Log.msg(TAG, "onFailure: -1- **")
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            Log.msg(TAG, "onFailure: -2- **")
                            if (EnrollActivity.fa == null) {
                                Log.msg(TAG, "onFailure: -3- va mostrar la Activity de Enrolamiento.")
                                showActivity(mContext, EnrollActivity::class.java)
                                Log.msg(TAG, "onFailure: -4- **")
                            }
                            e.startResolutionForResult(EnrollActivity.fa, Cons.REQUEST_CHECK_SETTINGS)
                            Log.msg(TAG, "onFailure: -5- **")
                        } catch (sendEx: SendIntentException) {
                            ErrorMgr.guardar(TAG, "onFailure", sendEx.message)
                            // Ignore the error.
                        }
                    } else Log.msg(TAG, "onFailure: no es - ResolvableApiException")
                } catch (ex: Exception) {
                    ErrorMgr.guardar(TAG, "onFailure", ex.message)
                }
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "createLocationRequest_P", ex.message)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requireLocation() {
        Log.msg(TAG, "[requireLocation]")
        try {
            val currentLocationTask = fusedLocationClient!!.getCurrentLocation(LocationRequest.QUALITY_HIGH_ACCURACY, cancellationTokenSource.token)
            currentLocationTask.addOnSuccessListener(locationListener())
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "requireLocation", ex.message)
        }
    }

    private val permisions: Boolean
        private get() {
            var bResult = false
            try {
                if (ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ) {
                    Log.msg(TAG, "ES NECESARIO PEDIR LOS PERMISOS DE LOCATION.")
                    bResult = true
                }
            } catch (ex: Exception) {
                ErrorMgr.guardar(TAG, "getPermisions", ex.message)
            }
            return bResult
        }

    fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            mContext!!, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            mContext!!, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        if (fineLocationPermissionState != PackageManager.PERMISSION_GRANTED) Log.msg(TAG, "ACCESS_FINE_LOCATION no Granted")
        if (backgroundLocationPermissionState != PackageManager.PERMISSION_GRANTED) Log.msg(TAG, "ACCESS_BACKGROUND_LOCATION no Granted")
        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

   // companion object {

//    }


}