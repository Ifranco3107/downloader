package com.macropay.downloader.utils.location

import android.Manifest
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import com.macropay.data.logs.Log.i
import com.google.android.gms.location.FusedLocationProviderClient
import android.os.Looper
import com.google.android.gms.location.LocationServices
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.macropay.downloader.receivers.LocationReceiver
import android.content.pm.PackageManager
import android.os.Handler
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationRequest
import com.macropay.downloader.ui.provisioning.Permissions
import java.lang.Exception

class LocationMgr(private val mContext: Context) {
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    init {
        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            msg(TAG, "**** Sin Permisos...***")
            //    Utils.autoGrantRequestedPermissions(context);
            try {
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({ showRequestPermissions(mContext) }, 1000)
            } catch (ex: Exception) {
                guardar(TAG, "LocationMgr[1]", ex.message)
            }
        }
        if (!checkPermissions()) {
            msg(TAG, "++++ Sin Permisos... +++")

        }
        try {
            msg(TAG, "Inicializa el GPS - getFusedLocationProviderClient..")
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
            configLocationRequest()
        } catch (ex: Exception) {
            guardar(TAG, "LocationMgr[2]", ex.message)
        }
    }

    fun showRequestPermissions(context: Context) {
        try {
            val intentMain = Intent(context, Permissions::class.java)
            msg(TAG, "showRequestPermissions -1- ")
            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentMain)
            msg(TAG, "showRequestPermissions -2- ")
        } catch (ex: Exception) {
            msg(TAG, "Error: " + ex.message)
        }
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun configLocationRequest() {
        msg(TAG, "configLocationRequest")
        mLocationRequest = LocationRequest()
        // Define el intervalo deseado para activar la actualizacion de ubicacion
        // Esta intervalo es inexacto,
        // Podrias no recibir todas la actualizaciones si las fuentes de Location no estas disponibles,
        // o podrias recibirlas mas lento de lo requerido,
        // Podrias recibirlas mas rapido de lo requerido, si alguna otra aplciacion requiere la ubicacion. antes del intervalo.
        //NOTA;
        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest!!.interval = UPDATE_INTERVAL

        // Sets the fastest rate for active location updates.
        // This interval is exact, and your application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval = FASTEST_UPDATE_INTERVAL
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        msg(TAG, "configLocationRequest : PRIORITY_BALANCED_POWER_ACCURACY")
        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest!!.maxWaitTime = MAX_WAIT_TIME
    }
    // Note: for apps targeting API level 25 ("Nougat") or lower, either
    // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting location updates.
    // For apps targeting API level O, only PendingIntent.getBroadcast() should be used.
    // This is due to the limits placed on services started in the background in "O".

    // TODO(developer): uncomment to use PendingIntent.getService().
    //Opcion Service, para API menor a 25, puede ser Service o Broadcast
    /*  Intent intent = new Intent(this.mContext, LocationReceiver.class);
         intent.setAction(LocationReceiver.ACTION_PROCESS_UPDATES);
         return PendingIntent.getService(this.mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/



    // Note: for apps targeting API level 25 ("Nougat") or lower, either
    // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting location updates.
    // For apps targeting API level O, only PendingIntent.getBroadcast() should be used.
    // This is due to the limits placed on services started in the background in "O".

    // TODO(developer): uncomment to use PendingIntent.getService().
    //Opcion Service, para API menor a 25, puede ser Service o Broadcast
    /*  Intent intent = new Intent(this.mContext, LocationReceiver.class);
         intent.setAction(LocationReceiver.ACTION_PROCESS_UPDATES);
         return PendingIntent.getService(this.mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);*/


    //Opcion Receiver, para API O, solo debe ser usado Broadcast
    private val pendingIntent: PendingIntent?
        private get() {
            //Opcion Receiver, para API O, solo debe ser usado Broadcast
            var pendingIntent: PendingIntent? = null
            try {
                val intent = Intent(mContext, LocationReceiver::class.java)
                intent.action = LocationReceiver.ACTION_PROCESS_UPDATES
                pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            } catch (ex: Exception) {
                guardar(TAG, "getPendingIntent", ex.message)
            }
            return pendingIntent
        }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    fun requestLocationUpdates() {
        try {
            if (checkPermissions()) {
                // Utils.setRequestingLocationUpdates(this, true);
                i(TAG, "[requestLocationUpdates] Iniciando location updates")
                mFusedLocationClient!!.requestLocationUpdates(mLocationRequest!!, pendingIntent!!) //requestLocationUpdates
            }
            else
                msg(TAG, "[requestLocationUpdates] NO TIENE PERMISOS")
        } catch (e: SecurityException) {
            // Utils.setRequestingLocationUpdates(this, false);
            guardar(TAG, "requestLocationUpdates", e.message)
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    fun removeLocationUpdates() {
        i(TAG, "[removeLocationUpdates] Removing location updates")
        //Utils.setRequestingLocationUpdates(this, false);
        mFusedLocationClient!!.removeLocationUpdates(pendingIntent!!)
    }

    /**
     * Return the current state of the permissions needed.
     */
    fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            mContext, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )


/*
        if(fineLocationPermissionState != PackageManager.PERMISSION_GRANTED)
            Log.msg(TAG,"ACCESS_FINE_LOCATION no Granted");


        if(backgroundLocationPermissionState != PackageManager.PERMISSION_GRANTED)
            Log.msg(TAG,"ACCESS_BACKGROUND_LOCATION no Granted");
*/


/*        return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED);*/return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
    } /*  public void requestPermissions() {

        boolean permissionAccessFineLocationApproved =
                ActivityCompat.checkSelfPermission(
                        this.mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean backgroundLocationPermissionApproved =
                ActivityCompat.checkSelfPermission(
                        this.mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        boolean shouldProvideRationale = permissionAccessFineLocationApproved && backgroundLocationPermissionApproved;

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displeiga un contexto adicional, para pedir permisos.  - Displaying permission rationale to provide additional context.");
            Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

        )

                            // Request permission
                            ActivityCompat.requestPermissions(this,
                                    new String[] {
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    })
                    .show();
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission.
            // It's possible this can be auto answered if device policy sets the permission in a given state
            // or the user denied the permission previously and checked "Never ask again".

          ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
*/

    /**
     * Callback received when a permissions request has been completed.
     */
    /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");

            } else if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission was granted.
                requestLocationUpdates(null);

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                        findViewById(R.id.activity_main),
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }*/
    companion object {
        private const val TAG = "LocationMgr" //LocationMgr.class.getSimpleName();
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value, but they may be less frequent.
         */
        private const val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds

        /**
         * The max time before batched results are delivered by location services. Results may be
         * delivered sooner than this interval.
         */
        private const val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.
    }
}