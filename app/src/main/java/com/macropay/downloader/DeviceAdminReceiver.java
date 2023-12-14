/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.macropay.downloader;

import android.annotation.TargetApi;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;

import com.macropay.downloader.data.preferences.dpcValues;
import com.macropay.downloader.di.Inject;
import com.macropay.downloader.ui.common.mensajes.ToastDPC;
import com.macropay.utils.broadcast.Sender;
import com.macropay.utils.preferences.Cons;
import com.macropay.downloader.domain.usecases.provisioning.Provisioning;
import com.macropay.downloader.utils.Settings;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
import com.macropay.downloader.utils.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {
    private static final String TAG = "DeviceAdminReceiver";
    static PersistableBundle bundleProvision;
    private int mUserId = 0;

    public static PersistableBundle getBundleProvision() {
        return bundleProvision;
    }

    public static void setBundleProvision(PersistableBundle bundle) {
        try{

            if(bundle == null)
                Log.INSTANCE.msg(TAG,"[setBundleProvision] bundle == NULL");
            else
                Log.INSTANCE.msg(TAG,"[setBundleProvision] bundle: OK");

            bundleProvision = bundle;
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"setBundleProvision",ex.getMessage());
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.INSTANCE.msg(TAG, "[onReceive] _______________________________________________");
            Log.INSTANCE.msg(TAG, "[onReceive] intent.getAction() [" + intent.getAction() +"]");
            String strNivel = "";
            switch (intent.getAction()) {

                case "android.app.action.DEVICE_ADMIN_ENABLED":
                    Log.INSTANCE.msg(TAG, "[onReceive] - DEVICE_ADMIN_ENABLED ");
                 //   dpcValues.INSTANCE.setProvisioning(true);
                    break;
                // PROFILE_PROVISIONING_COMPLETE:
                case DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE:
                    Log.INSTANCE.msg(TAG, "[onReceive] - ACTION_PROVISION_MANAGED_PROFILE ");
                    break;
                case DevicePolicyManager.ACTION_PROFILE_OWNER_CHANGED:
                    //No se usa
                    Log.INSTANCE.msg(TAG, "[onReceive] PROFILE OWNER");
                    onProfileOwnerChanged(context);
                    break;

                case DevicePolicyManager.ACTION_DEVICE_OWNER_CHANGED:
                    Boolean isOwner = Utils.Companion.isDeviceOwner(context);
                    Log.INSTANCE. init("downloader", context);
                    Log.INSTANCE.msg(TAG, "[onReceive] Aqui esta entrando con Android 12");
                    Log.INSTANCE.msg(TAG, "[onReceive] DEVICE OWNER: "+isOwner);
                    Log.INSTANCE.msg(TAG, "[onReceive] Version: " + Utils.SDK_INT);
                    dpcValues.INSTANCE.setProvisioning(true);

                    //TODO: Aqui entra cuando estan las activities de Android 12,
                    // Android 12 [31]
                    if(isOwner){
                        String support12 = Utils.getAppValue(context );
                        Log.INSTANCE.msg(TAG,"[onReceive] ---------------------------------------------");
                        Log.INSTANCE.msg(TAG,"[onReceive] ---------------------------------------------");
                        Log.INSTANCE.msg(TAG,"[onReceive] ---------------------------------------------");
                     //   Log.INSTANCE.msg(TAG,"[onReceive] supportAndroid12: ["+support12 +"]");
                        Log.INSTANCE.msg(TAG,"[onReceive] SDK Version: "+Utils.SDK_INT);
                        Boolean newProvisioningMode = Settings.INSTANCE.getSetting(Cons.NEW_PROVISIONG_MODE,false);
                        Log.INSTANCE.msg(TAG,"[onReceive] newProvisioningMode: "+newProvisioningMode);

                        Log.INSTANCE.msg(TAG,"[onReceive] Inicia proceso de instalacion... ");
                        //14Sept2023 --
                      //  Inject.INSTANCE.inject().getProvision().start(intent);

                        //Solo para notificar que ya termino el Provisioning.
                        Sender.INSTANCE.sendEnrollProcess(context,true,300,"");

                    }
                    break;

                case ACTION_PROFILE_PROVISIONING_COMPLETE:
                    Log.INSTANCE.msg(TAG,"[onReceive] SDK Version: "+Utils.SDK_INT);
                    dpcValues.INSTANCE.setProvisioning(true);
                    //Android 11 [30] R
                    //Android 10 [29] Q
                    //Android 9 [28] P
                    if (Utils.SDK_INT < VERSION_CODES.R) {
                        Log.INSTANCE.msg(TAG,"[onReceive] =+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
                        Log.INSTANCE.msg(TAG,"[onReceive] =+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
                        Log.INSTANCE.msg(TAG,"[onReceive] =+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+=+");
                        Log.INSTANCE.msg(TAG,"[onReceive] Aqui entra en el flujo normal,Android  <= 11");

/*                        Provisioning provisioning = Inject.INSTANCE.inject().getProvision();
                        provisioning.start(intent);*/
                        Inject.INSTANCE.inject().getProvision().start(intent);
                    }
                    break;
                case "android.app.action.TRANSFER_OWNERSHIP_COMPLETE":
                    Log.INSTANCE.msg(TAG,"[onReceive] EJECUTO EN: TRANSFER_OWNERSHIP_COMPLETE ");
                    Log.INSTANCE.msg(TAG,"[onReceive] PO asignado manual, para paguitos");

                    PersistableBundle bundle = intent.getParcelableExtra(EXTRA_TRANSFER_OWNERSHIP_ADMIN_EXTRAS_BUNDLE);
                    onTransferOwnershipComplete( context, bundle);
                    break;
                case "android.app.action.NOTIFY_PENDING_SYSTEM_UPDATE":
                    Log.INSTANCE.msg(TAG,"[onReceive] Tiene update SYSTEM PENDINETE...");
                    break;

                case ACTION_LOCK_TASK_ENTERING:
                    Log.INSTANCE.msg(TAG,"[onReceive] ACTION_LOCK_TASK_ENTERING...");
                    try{
                        Settings.INSTANCE.setSetting("kioskoShowed",true);
                    }catch (Exception ex){
                        ErrorMgr.INSTANCE.guardar(TAG,"[onReceive] ACTION_LOCK_TASK_ENTERING",ex.getMessage());
                    }
                    break;

                case ACTION_LOCK_TASK_EXITING:
                    try{
                        Log.INSTANCE.msg(TAG,"[onReceive] ACTION_LOCK_TASK_EXITING...");
                        Settings.INSTANCE.setSetting("kioskoShowed",false);
                    }catch (Exception ex){
                        ErrorMgr.INSTANCE.guardar(TAG,"[onReceive] ACTION_LOCK_TASK_EXITING",ex.getMessage());
                    }
                    Log.INSTANCE.msg(TAG,"[onReceive] ACTION_LOCK_TASK_EXITING... ok");
                    break;

                default:
                    Log.INSTANCE.msg(TAG, "[onReceive] DEFAULT");
                    super.onReceive(context, intent);
                    break;
            }
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "onReceive", ex.getMessage());
        }
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        try {
            Log.INSTANCE.msg(TAG,"[onEnabled] - ========================= ");
            Log.INSTANCE.msg(TAG,"[onEnabled] - =       inicio          = ");
            Log.INSTANCE.msg(TAG,"[onEnabled] - ========================= ");
            ToastDPC.showToast(context,"onEnabled -1-");

/*            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            long serialNumber = userManager.getSerialNumberForUser(Binder.getCallingUserHandle());*/
          //  Log.i(TAG ,"[onEnabled] Device admin enabled in user with serial number: " + serialNumber);
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG, "onEnabled", ex.getMessage());
        }
    }

   // @TargetApi(VERSION_CODES.P)  createAndManageUser
  //  public void onTransferOwnershipComplete(Context context, Intent bundle) {
   public void onTransferOwnershipComplete(Context context, PersistableBundle bundle) {
        Log.INSTANCE.i(TAG, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
        Log.INSTANCE.i(TAG, "&&                     onTransferOwnershipComplete                           && ");
        Log.INSTANCE.i(TAG, "&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");

        //Paguitos..
//TODO
       Settings.INSTANCE.setSetting("isEnroladoManual",true);
        Intent intent = new Intent();
        intent.putExtra("bundleManual", bundle);
        Log.INSTANCE.msg(TAG,"va ejecutar --> onProfileProvisioningComplete: " );
        Log.INSTANCE.msg(TAG,"isEnroladoManual: "+Settings.INSTANCE.getSetting("isEnroladoManual",false));
       //nuevo esquema
       Provisioning provisioning = new Provisioning(context);
       provisioning.start(intent);
       // onProfileProvisioningComplete(context,intent);
   }
    //
    private void onProfileOwnerChanged(Context context) {
        Log.INSTANCE.i(TAG, "onProfileOwnerChanged - Profile Onwer - Work Owner - managed profile -  PERFIL DE TRABAJO");
    }

    private void onDeviceOwnerChanged(Context context) {
        Log.INSTANCE.i(TAG, "onDeviceOwnerChanged - PERFIL PERSONAL");
    }

    @TargetApi(VERSION_CODES.M)
    @Override
    public void onSystemUpdatePending(Context context, Intent intent, long receivedTime) {
        if (receivedTime != -1) {
            DateFormat sdf = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");
            String timeString = sdf.format(new Date(receivedTime));
            Log.INSTANCE.msg(TAG, "[onSystemUpdatePending] Actualizacion requerida del Dispositivo: " + timeString);

        } else {
            Log.INSTANCE.msg(TAG, "[onSystemUpdatePending] No system update is currently available on this device.");
        }

    }

    @TargetApi(VERSION_CODES.P)
    @Override
    public void onUserStarted(Context context, Intent intent, UserHandle startedUser) {
        try{
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            long userID =   userManager.getSerialNumberForUser(startedUser);
            Log.INSTANCE.msg(TAG,"onUserStarted: "+userID );
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"onUserStarted",ex.getMessage());
        }
        //  handleUserAction(context, startedUser, R.string.on_user_started_title,
        //          R.string.on_user_started_message, NotificationUtil.USER_STARTED_NOTIFICATION_ID);
    }

    @TargetApi(VERSION_CODES.P)
    @Override
    public void onUserStopped(Context context, Intent intent, UserHandle stoppedUser) {
        try{
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            long userID =   userManager.getSerialNumberForUser(stoppedUser);
            Log.INSTANCE.msg(TAG,"onUserStopped: "+userID );
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"onUserStopped",ex.getMessage());
        }

    }

    @TargetApi(VERSION_CODES.P)
    @Override
    public void onUserSwitched(Context context, Intent intent, UserHandle switchedUser) {
        try{
            UserManager userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
            long userID =   userManager.getSerialNumberForUser(switchedUser);
            Log.INSTANCE.msg(TAG,"switchedUser: "+userID );
        }catch (Exception ex){
            ErrorMgr.INSTANCE.guardar(TAG,"switchedUser",ex.getMessage());
        }
/*        handleUserAction(context, switchedUser, R.string.on_user_switched_title,
                R.string.on_user_switched_message, NotificationUtil.USER_SWITCHED_NOTIFICATION_ID);*/
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdminReceiver.class);
    }

    @TargetApi(VERSION_CODES.P)
    public void onTransferAffiliatedProfileOwnershipComplete(Context context, UserHandle user) {
        Log.INSTANCE.i(TAG, "onTransferAffiliatedProfileOwnershipComplete");
    }



}
