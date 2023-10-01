package com.macropay.downloader.ui.provisioning

import android.Manifest
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import androidx.appcompat.app.AppCompatActivity
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.macropay.downloader.databinding.ActivityPermissionsBinding
import com.macropay.downloader.utils.SettingsApp
import com.macropay.downloader.utils.Utils
import com.macropay.downloader.utils.policies.Restrictions
import java.lang.Exception

/*import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;*/
class Permissions : AppCompatActivity() {
    private var binding: ActivityPermissionsBinding? = null
    var TAG = "Permissions"

    //  Context mContext = null;
    var mActivity: Activity? = null
   // var accessibilityServiceClass: Class<*> = VolumeAccessibilityService::class.java
    var bAccessibility = false
    var bDrawOverlay = false
    var bUsageApps = false
    var bLocation = false
    var intentoLocation = 0
    val REQUEST_CODE_ACCESIBILITY = 2000
    val REQUEST_CODE_OVELAY = 2001
    val REQUEST_CODE_LOCATION = 2002
    val REQUEST_CODE_USAGE_APPS = 2003
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        msg(TAG, "onCreate -1-")
        binding = ActivityPermissionsBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)
        mActivity = this
        Asignar()
    }

    fun Asignar(): Boolean {
        //accessibilityPermission();
        //DrawOverlayPermission();
        try {
            LocationPermission()
            usagePermission()
            validateResults()
        } catch (ex: Exception) {
            guardar(TAG, "Asignar.**", ex.message)
        }
        return true
    }

    //Accesibility permissions
    fun usagePermission() {
        bUsageApps = isUsageApssGranted
        if (bUsageApps) return
        try {
            msg(TAG, "[usagePermission] -1- ")
            val packageName = this.packageName
            msg(TAG, "[usagePermission] getPackageName: [$packageName]")
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.fromParts("package", packageName, null)
            /*            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);*/launchSomeActivity.launch(intent)
        } catch (ex: Exception) {
            guardar(TAG, "usagePermission", ex.message)
        }
    }

    /*
        //Accesibility permissions
        private void accessibilityPermission(){

        bAccessibility =  isAccessServiceEnabled();
        if (bAccessibility) return;
        try{
            Log.msg(TAG,"accessibilityPermissions -1- ");
            Intent intentPerms = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intentPerms.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            Log.msg(TAG,"accessibilityPermissions -2- ");
           // launchSomeActivity.launch(intentPerms);
            Log.msg(TAG,"accessibilityPermissions -4- ");
        }
        catch (Exception ex){
            ErrorMgr.guardar(TAG,"accessibilityPermissions",ex.getMessage(),false);
        }
    }*/
    //Overlay permssions
    /*    private void DrawOverlayPermission(){
        bDrawOverlay= isDrawOverlayEnabled();
        if (bDrawOverlay) return;
        try{
            Log.msg(TAG,"DrawOverlayPermission -1- ");
            Intent intentDrawOverlay = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            intentDrawOverlay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
         //   launchSomeActivity.launch(intentDrawOverlay);
        }
        catch (Exception ex){
            ErrorMgr.guardar(TAG,"DrawOverlayPermission",ex.getMessage(),false);
        }

    }*/
    private fun LocationPermission() {
        msg(TAG, "LocationPermission")
        var backgroundLocationApproved = true
        val AccessFineLocationApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val AccessCoarseLocationApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (Utils.SDK_INT >= Build.VERSION_CODES.Q) backgroundLocationApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        val requierePermisos = AccessFineLocationApproved && backgroundLocationApproved
        msg(TAG, "AccessFineLocationApproved: $AccessFineLocationApproved")
        msg(TAG, "AccessCoarseLocationApproved: $AccessCoarseLocationApproved")
        msg(TAG, "backgroundLocationApproved: $backgroundLocationApproved")
        if (!requierePermisos) {
            pidePermisosGPS()
        }
    }

    private fun pidePermisosGPS() {
        if (Utils.SDK_INT >= Build.VERSION_CODES.Q) {
            msg(TAG, "Version: " + Utils.SDK_INT)
            val permisos = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) //Required only when requesting background location access on Android 10 (API level 29) and higher
            ActivityCompat.requestPermissions(this, permisos, REQUEST_CODE_LOCATION)
        } else {
            val permisos = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(this, permisos, REQUEST_CODE_LOCATION)
        }
    }

    //
    /*  private void LocationPermission(){
            Log.msg(TAG,"LocationPermission");
    
    
    
            boolean AccessFineLocationApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean backgroundLocationApproved = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    
            boolean shouldProvideRationale = AccessFineLocationApproved && backgroundLocationApproved;
    
            // Provide an additional rationale to the user. This would happen if the user denied the
            // request previously, but didn't check the "Don't ask again" checkbox.
            if (shouldProvideRationale) {
                Log.i(TAG, "Despliega un contexto adicional, para pedir permisos.  - Displaying permission rationale to provide additional context.");
                Snackbar.make(
                        binding.getRoot(),
                        "ddd",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Aceptar", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
    
               ///
    
                                // Request permission
                                ActivityCompat.requestPermissions(Permissions.this,
                                        new String[] {
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION },
                                        REQUEST_CODE_LOCATION);
                            }
                        })
                        .show();
            } else {
                Log.i(TAG, "requestPermissions - 1 -");
                // Request permission.
                // It's possible this can be auto answered if device policy sets the permission in a given state
                // or the user denied the permission previously and checked "Never ask again".
                ActivityCompat.requestPermissions(Permissions.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
    
    */
    /*            ActivityCompat.requestPermissions(this,
                        new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                        REQUEST_PERMISSIONS_REQUEST_CODE);*/
    /*
                Log.i(TAG, "requestPermissions -2-");
            }
        }*/
    /* private void LocationPermission(){
            bLocation= isLocationEnabled();
            if (bLocation) return;
    
            try{
                Log.msg(TAG,"LocationPermission -1- VA PEDIR PERMISO.");
               List<String> missingPermissions = new ArrayList<>();
                missingPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
                ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), PERMISSIONS_CODE);
                long espera = 1000;
                if (intentoLocation > 0)
                    espera = 60000;
    
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                        ActivityCompat.requestPermissions(mActivity, permissions, PERMISSIONS_CODE);
    
                        Log.msg(TAG,"LocationPermission -2- PIDIO PERMISO.");
                        intentoLocation++;
                    }
                }, espera);
    
    
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // In an educational UI, explain to the user why your app requires this
                    // permission for a specific feature to behave as expected. In this UI,
                    // include a "cancel" or "no thanks" button that allows the user to
                    // continue using your app without granting the permission.
                    //showInContextUI();
    
    
                } else {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    //  requestPermissionLauncher.launch(
                    //          Manifest.permission.REQUESTED_PERMISSION);
    
                    requestPermissionLauncher.launch( Manifest.permission.ACCESS_FINE_LOCATION);
                }
    
    
            }
            catch (Exception ex){
                ErrorMgr.guardar(TAG,"LocationPermission",ex.getMessage(),false);
            }
        }*/
    val isUsageApssGranted: Boolean
        get() {
            val packageName = packageName
            val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager
            var mode = 0
            mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), packageName)
            val granted = mode == AppOpsManager.MODE_ALLOWED
            msg(TAG, "packageName: [$packageName] granted: $granted")
            return granted
        }

    /*                Log.msg(TAG,"isAccessServiceEnabled prefString= "+prefString);
                Log.msg(TAG,"isAccessServiceEnabled  accessibilityServiceClass.getName()= "+ accessibilityServiceClass.getName());
                Log.msg(TAG,"isAccessServiceEnabled  this.getPackageName() = "+ this.getPackageName() );*/
/*    val isAccessServiceEnabled: Boolean
        get() {
            var bEnabled = false
            try {
                val prefString = Settings.Secure.getString(this.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                if (prefString != null) {
*//*                Log.msg(TAG,"isAccessServiceEnabled prefString= "+prefString);
                Log.msg(TAG,"isAccessServiceEnabled  accessibilityServiceClass.getName()= "+ accessibilityServiceClass.getName());
                Log.msg(TAG,"isAccessServiceEnabled  this.getPackageName() = "+ this.getPackageName() );*//*
                    bEnabled = prefString.contains(this.packageName + "/" + accessibilityServiceClass.name)
                }
            } catch (ex: Exception) {
                guardar(TAG, "isAccessServiceEnabled", ex.message)
            }
            msg(TAG, "isAccessServiceEnabled bEnabled= $bEnabled")
            return bEnabled
        }*/
    val isDrawOverlayEnabled: Boolean
        get() {
            var bEnabled = false
            try {
                bEnabled = Settings.canDrawOverlays(this)
            } catch (ex: Exception) {
                guardar(TAG, "isDrawOverlayEnabled", ex.message)
            }
            msg(TAG, "isDrawOverlayEnabled bEnabled= $bEnabled")
            return bEnabled
        }
    val isLocationEnabled: Boolean
        get() {
            var bEnabled = false
            try {
                bEnabled = Settings.canDrawOverlays(this)
                bEnabled = (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            } catch (ex: Exception) {
                guardar(TAG, "isLocationEnabled", ex.message)
            }
            msg(TAG, "isLocationEnabled bEnabled= $bEnabled")
            return bEnabled
        }

    //------------------------------
    // onRequestPermissionsResult
    //Resultado de la peticion de Permisos
    var launchSomeActivity = registerForActivityResult<Intent, ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        msg(TAG, "onActivityResult: " + result.resultCode)
        //validateResults();
    }

    /*
    ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            });*/
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_LOCATION) {
            msg(TAG, "onRequestPermissionsResult")
            for (i in permissions.indices) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                msg(TAG, i.toString() + ".- " + permission + " grant: " + (grantResult == PackageManager.PERMISSION_GRANTED))
                if (permission == Manifest.permission.ACCESS_FINE_LOCATION) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        //  onPPSButtonPress();
                    }
                }
            }

            //Verifica que se hayan asignado los permisos...
            //    validateResults();
        }
    }

    private fun validateResults(): Boolean {
        msg(TAG, "------------------- validateResults ------------------------------")
        //    bAccessibility =  isAccessServiceEnabled();
        //    bDrawOverlay= isDrawOverlayEnabled();
        bLocation = isLocationEnabled
        //
        bUsageApps = isUsageApssGranted
        //  Log.msg(TAG,"validateResults :bAccessibility= " +bAccessibility);
        //  Log.msg(TAG,"validateResults :bDrawOverlay= " +bDrawOverlay);
        msg(TAG, "validateResults :bLocation= $bLocation")

        /* if (!bAccessibility)
        {
            accessibilityPermission();
            return false;
        }
        if (!bDrawOverlay)
        {
            DrawOverlayPermission();
            return false;
        }*/if (!bLocation) {
            LocationPermission()
            return false
        }
        if (!bUsageApps) {
            usagePermission()
            return false
        }

        // if (result.getResultCode() == Activity.RESULT_OK) {&&  bLocation&&  bLocation
        // if(bAccessibility &&bDrawOverlay ) {
        //Si todos los permisos fueron asigados, termina esta Activity (Permisos)
        val allAsigned = bLocation && bUsageApps
        // if (bLocation) {
        if (allAsigned) {
            msg(TAG, "*** PERMISOS ASIGNADOS ***")
            // Intent data = result.getData(); //Para info de retorto
            SettingsApp.setPermissionsAsigned(true)
            if (SettingsApp.canReboot()) {
                msg(TAG, "REBOOT")
                msg(TAG, ".")
                Reboot()
            } else {
                msg(TAG, "FINISH")
                terminarDlg()
            }
        }
        return allAsigned
        //return bAccessibility &&bDrawOverlay ;
    }

    private fun terminarDlg() {
        //Informacion envida de retorno, como resultado.
        val returnIntent = Intent()
        returnIntent.putExtra("result", 1)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    fun Reboot() {
        try {
            val restrictions = Restrictions(this)
            restrictions.Reboot()
        } catch (ex: Exception) {
            guardar(TAG, "Reboot", ex.message)
        }
    }

    override fun onBackPressed() {
        msg(TAG, "onBackPressed - bloqueado ")
        //  bAccessibility =  isAccessServiceEnabled();
        //  bDrawOverlay= isDrawOverlayEnabled();
        //  Log.msg(TAG, "onBackPressed - bAccessibility " +bAccessibility);
        //  Log.msg(TAG, "onBackPressed - bDrawOverlay " +bDrawOverlay);
        bLocation = isLocationEnabled
        msg(TAG, "onBackPressed - bLocation $bLocation")
        //    if(!bAccessibility || !bDrawOverlay ) {
/*        if(!bLocation ) {
            Log.msg(TAG, "onBackPressed - bloqueado ");
            return;
        }
        else*/super.onBackPressed()
    }
}