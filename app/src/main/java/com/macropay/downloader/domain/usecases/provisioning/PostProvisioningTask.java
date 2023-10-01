/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.macropay.downloader.domain.usecases.provisioning;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
//import android.util.Log;

import com.macropay.downloader.DeviceAdminReceiver;

import com.macropay.downloader.ui.provisioning.FinalizeActivity;
import com.macropay.downloader.utils.Utils;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;

import java.util.ArrayList;
import java.util.List;

import static android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE;
import static android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED;



public class PostProvisioningTask {
    private static final String TAG = "PostProvisioningTask";
    //private static final String SETUP_MANAGEMENT_LAUNCH_ACTIVITY = "com.afwsamples.testdpc.SetupManagementLaunchActivity";
    private static final String SETUP_MANAGEMENT_LAUNCH_ACTIVITY = "com.macropay.downloader.MacroPoliciesActivity";

    private static final String POST_PROV_PREFS = "post_prov_prefs";
    private static final String KEY_POST_PROV_DONE = "key_post_prov_done";

    private  Context mContext;
    private  DevicePolicyManager mDevicePolicyManager;
    private  SharedPreferences mSharedPrefs;

    public PostProvisioningTask(Context context) {
        Log.INSTANCE.init("downloader",context);
        Log.INSTANCE.msg(TAG,"Constructor - - ");
        ErrorMgr.INSTANCE.init(context);
        try{
            mContext = context;

            mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            mSharedPrefs = context.getSharedPreferences(POST_PROV_PREFS, Context.MODE_PRIVATE);
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"PostProvisioningTask",ex.getMessage());
        }

    }

   // public boolean performPostProvisioningOperations(Intent intent) {
        public boolean performPostProvisioningOperations() {
       // Log.INSTANCE.msg(TAG,"performPostProvisioningOperations: ---> "+ intent.getAction());
        try{
            if (isPostProvisioningDone()) {
                Log.INSTANCE.msg(TAG,"isPostProvisioningDone (RETURN FALSE)");

                return false;
            }

            markPostProvisioningDone();
            Log.INSTANCE.msg(TAG,"-- Android "+ Build.VERSION.RELEASE+" - sdk - "+ Utils.SDK_INT + " autoGrantRequestedPermissionsToSelf");
          //  autoGrantRequestedPermissionsToSelf();
            Log.INSTANCE.msg(TAG,"-- Termino OK - autoGrantRequestedPermissionsToSelf");

        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"performPostProvisioningOperations",ex.getMessage());
        }

/*
        // Retreive the admin extras bundle, which we can use to determine the original context for TestDPCs launch.
        PersistableBundle extras = intent.getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        if (Util.SDK_INT >= VERSION_CODES.O) {
             Log.INSTANCE.msg(TAG,"2 -- Android "+ Build.VERSION.RELEASE+" - sdk - " +Util.SDK_INT+ " maybeSetAffiliationIds");

             //maybeSetAffiliationIds(extras);
        }
*/

/*
        // Hide the setup launcher when this app is the admin
       mContext.getPackageManager().setComponentEnabledSetting(
                new ComponentName(mContext, SETUP_MANAGEMENT_LAUNCH_ACTIVITY),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
*/
        Log.INSTANCE.msg(TAG,"termino (RETURN TRUE)");
        return true;
    }

    public Intent getPostProvisioningLaunchIntent(Intent intent) {
  //      public Intent getPostProvisioningLaunchIntent(Intent intent) {
        // Enable the profile after provisioning is complete.
        Intent launch = null;

        PersistableBundle extras = intent.getParcelableExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE);
        String packageName = mContext.getPackageName();

        boolean isProfileOwner = mDevicePolicyManager.isProfileOwnerApp(packageName);
        boolean isDeviceOwner = mDevicePolicyManager.isDeviceOwnerApp(packageName);

        Log.INSTANCE.msg(TAG,"[getPostProvisioningLaunchIntent] isProfileOwner: "+isProfileOwner);
        Log.INSTANCE.msg(TAG,"[getPostProvisioningLaunchIntent] isDeviceOwner: "+isDeviceOwner);

        // Drop out quickly if we're neither profile or device owner.
        if (!isProfileOwner && !isDeviceOwner) {
            Log.INSTANCE.msg(TAG,"[getPostProvisioningLaunchIntent] - SE SALIO... return null ");
            return null;
        }
        Log.INSTANCE.msg(TAG,"[getPostProvisioningLaunchIntent] asigno --> FinalizeActivity ");
        launch = new Intent(mContext, FinalizeActivity.class);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        launch.putExtra(EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, extras);
        return launch;
    }


    private void markPostProvisioningDone() {
        try{
            mSharedPrefs.edit().putBoolean(KEY_POST_PROV_DONE, true).commit();
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"",ex.getMessage());
        }

    }

    private boolean isPostProvisioningDone() {
        return mSharedPrefs.getBoolean(KEY_POST_PROV_DONE, false);
    }

    @TargetApi(VERSION_CODES.M)
    private void autoGrantRequestedPermissionsToSelf() {
        String packageName = mContext.getPackageName();
        ComponentName adminComponentName = DeviceAdminReceiver.getComponentName(mContext);

        List<String> permissions = getRuntimePermissions(mContext.getPackageManager(), packageName);

        Log.INSTANCE.msg(TAG, "autoGrantRequestedPermissionsToSelf: " + permissions .size() + " Permisos.");
        int count = 0;
        for (String permission : permissions) {
            try {
                count++;
                Log.INSTANCE.msg(TAG, count+" Granting " + permission   );

                boolean success = mDevicePolicyManager.setPermissionGrantState(adminComponentName,
                        packageName, permission, PERMISSION_GRANT_STATE_GRANTED);
             //   Log.INSTANCE.msg(TAG, "Granting  success: " + success   );
                if (!success) {
                    Log.INSTANCE.e(TAG, "fallo al hacer  grant al permiso: " + permission);
                }
            }
            catch (SecurityException ex){
                ErrorMgr.INSTANCE.guardar(TAG,"autoGrantRequestedPermissionsToSelf["+permission +"]",ex.getMessage());
            }
        }
    }


    private List<String> getRuntimePermissions(PackageManager packageManager, String packageName) {
        List<String> permissions = new ArrayList<>();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            Log.INSTANCE.e(TAG, "Could not retrieve info about the package: " + packageName, e);
            return permissions;
        }

        if (packageInfo != null && packageInfo.requestedPermissions != null) {
            for (String requestedPerm : packageInfo.requestedPermissions) {
                if (isRuntimePermission(packageManager, requestedPerm)) {
                    permissions.add(requestedPerm);
                }
            }
        }
        return permissions;
    }

    private boolean isRuntimePermission(PackageManager packageManager, String permission) {
        try {
            PermissionInfo pInfo = packageManager.getPermissionInfo(permission, 0);
            if (pInfo != null) {
                if ((pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE)
                        == PermissionInfo.PROTECTION_DANGEROUS) {
                    return true;
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.INSTANCE.i(TAG, "No pudo extraer informacion del permiso: " + permission);
        }
        return false;



    }




    public void completeProvisioning() {

        ComponentName componentName = DeviceAdminReceiver.getComponentName(mContext);
        // This is the name for the newly created managed profile.}
       // mContext.getString(R.string.profile_name
        mDevicePolicyManager.setProfileName(componentName,"Perfil MacroLock");

        // We enable the profile here.
        mDevicePolicyManager.setProfileEnabled(componentName);
    }
}
