package com.macropay.downloader.utils.policies;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.app.admin.FactoryResetProtectionPolicy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;

import com.macropay.downloader.DeviceAdminReceiver;

import java.util.ArrayList;
import java.util.List;

import com.macropay.downloader.data.preferences.TipoParametro;
import com.macropay.downloader.utils.Utils;
import com.macropay.data.logs.ErrorMgr;
import com.macropay.data.logs.Log;
import com.macropay.utils.Settings;

/*
https://fasrlord275.weebly.com/how-to-use-test-dpc-to-bypass-frp.html

Enable enterprise factory reset protection

Using enterprise factory reset protection, organizations can specify which Google Accounts can provision a device that has been factory reset.
Consumer factory reset protection is designed to deter device theft. Before allowing anyone to provision the device after unauthorized factory reset(such as via an EMM), the setup wizard requires the user to authenticateagainst any Google Accounts that were previously on the personal profileof the device.
In an enterprise environment, factory reset is an important tool for managing employee devices when an employee leaves the organization. However, if theorganization doesn’t know an employee’s account credentials, factory resetprotection can block the organization’s ability to issue a device to anotheremployee.
Control provisioning after a factory reset
When running in device owner mode, your DPC can use factoryResetProtectionAdminto control which accounts are authorized to provision a device after a factoryreset. If this managed configuration isn’t present, is set to null or set toan empty list, the accounts authorized to provision a device after a factoryreset are the accounts currently on the personal profile of the device.
A DPC can configure these accounts throughout the lifetime of a fully manageddevice.
The DPC obtains the IDs of the accounts (manually or programmatically) that canprovision a device after a factory reset.
The DPC uses the special value 'me' as the userId to indicatethe authenticated user. The ID is returned as an integer string. Newly-createdaccounts might not be available for factory reset purposes for 72 hours.

The DPC sets an appropriate app restriction using DevicePolicyManager.setApplicationRestrictions()
to set the values of the key-value pair in the settings bundle and to indicate the package the restrictions are for:

Key name--factoryResetProtectionAdmin.
Key value—A string containing one account ID that can provision a factory resetdevice or a string array containing multiple account IDs.
Package name--com.google.android.gms.
The DPC enables the accounts that can provision devices after a factory resetby sending the broadcast com.google.android.gms.auth.FRP_CONFIG_CHANGED.

Disable factory reset protection
To disable factory reset protection, the DPC must set disableFactoryResetProtectionAdmin with a key value of true.
Simply leaving this managed configuration unset does not disable factory reset protection.

The DPC sets an appropriate app restriction using DevicePolicyManager.setApplicationRestrictions()to set the values of the key-value pair in the settings bundle andto indicate the package the restrictions are for:
Key name--disableFactoryResetProtectionAdmin.
Key value—a boolean value of true or false (default).
Package name--com.google.android.gms.

The DPC notifies Google Play services of the change by sending the broadcast com.google.android.gms.auth.FRP_CONFIG_CHANGED.
*/

//version   28 = Android 9 P
//          29 = 10 Q
//          30 = 11 R
//          31 = 12 S
public class FactoryReset extends Activity {

    private static final int DISABLED = 0;
    private static final int ENABLED = 1;

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponentName;


    private List<String> mAccounts = new ArrayList<String>();
    private boolean mEnabled;

    private String TAG = "FactoryReset";

    // public FactoryReset(Activity activity) {
    public FactoryReset(Context context) {
        this.attachBaseContext(context);
        mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = DeviceAdminReceiver.getComponentName(context);

    }



    public void addAccount(String account){
      //  Log.INSTANCE.msg("addAccount","account: "+account);
        mAccounts.add(account);
    }

    public boolean setProtection(boolean mEnabled){
        //
        //https://developers.google.com/people/api/rest/v1/people/get?apix_params
        //ResourceName = people/me
        //PersonFields = metadata
        Log.INSTANCE.msg(TAG,"Status actual:  FactoryReset("+mEnabled+")");
        //https://developers.google.com/people/api/rest/v1/people/get?apix_params=%7B%22resourceName%22%3A%22people%2Fme%22%2C%22personFields%22%3A%22metadata%22%7D
        //https://developers.google.com/people/api/rest/v1/people/get?apix_params
        //ResourceName = people/me
        //PersonFields = metadata
        //https://developers.google.com/people/api/rest/v1/people/get?apix_params=%7B%22resourceName%22%3A%22people%2Fme%22%2C%22personFields%22%3A%22metadata%22%7D
        String accountId = Settings.INSTANCE.getSetting(TipoParametro.google_account_id,"");
        Log.INSTANCE.msg(TAG,"[setProtection] accountId: "+accountId);

      /// addAccount("104845394971027357315"); //"ifranco3107@gmail.com"
        addAccount("103218677152651074546");/*macroplayp@gmail.com */
        if(!accountId.isEmpty()){
            Log.INSTANCE.msg(TAG,"[setProtection] agrego cuenta: "+accountId);
            addAccount(accountId);
        }else
        {
            if(mAccounts.size() == 0){
                Log.INSTANCE.msg(TAG,"no hay cuentas para registrar.");
                return false;
            }
        }


        if (Utils.SDK_INT >= Build.VERSION_CODES.R) {
            Log.INSTANCE.msg(TAG,"factoryReset.setProtection("+mEnabled +"); -- [R] SDK: "+Utils.SDK_INT);
            EnableFRP_R(mEnabled);
        }
        else {
            Log.INSTANCE.msg(TAG, "factoryReset.EnableFRP("+mEnabled +"); -- [Q]  SDK: " + Utils.SDK_INT);
            EnableFRP_Q(mEnabled);
        }
        return true;
    }

    //DevicePolicyManager.setApplicationRestrictions ()
    //   factoryResetProtectionAdmin
   @RequiresApi(api = Build.VERSION_CODES.R)
   public void EnableFRP_R(Boolean mEnabled ){
     //   Log.INSTANCE.msg(TAG, "EnableFRP_R --- mEnabled: "+mEnabled);
        try{

            if(!mEnabled) {
                Log.INSTANCE.msg(TAG,"[EnableFRP_R] inicializa mAccounts");
                mAccounts = new ArrayList<>();
            }
            Log.INSTANCE.msg(TAG, "[EnableFRP_R] ---  Cuentas : " + mAccounts.size());
            for (String account : mAccounts)
            {
                Log.INSTANCE.msg(TAG, "[EnableFRP_R] --- cuenta: "+ account);
            }

            FactoryResetProtectionPolicy policy = new FactoryResetProtectionPolicy.Builder()
                    .setFactoryResetProtectionAccounts(mAccounts)
                    .setFactoryResetProtectionEnabled(mEnabled)
                    .build();
            //policy.isFactoryResetProtectionEnabled();
         //   Log.INSTANCE.msg(TAG, "EnableFRP_R --- Creo le objeto --policy ");

            mDevicePolicyManager.setFactoryResetProtectionPolicy(mAdminComponentName, policy);
           // Log.INSTANCE.msg(TAG, "EnableFRP_R --- Asigno el objeto Polilcy ");

            FactoryResetProtectionPolicy result = mDevicePolicyManager.getFactoryResetProtectionPolicy(mAdminComponentName);

            if(result != null)
                Log.INSTANCE.msg(TAG, "EnableFRP_R --- Asigno FRP Correctamente-  result.isFactoryResetProtectionEnabled() =  "+ result.isFactoryResetProtectionEnabled());
            else
                Log.INSTANCE.msg(TAG, "EnableFRP_R --- NO SE ASIGNO frp.... ");

            // <!-- Added in R [30 R Android 11] -->
            //                <action android:name="android.app.action.RESET_PROTECTION_POLICY_CHANGED" />

     /*       verify(mContext.spiedContext).sendBroadcastAsUser(
                    MockUtils.checkIntentAction(
                            DevicePolicyManager.ACTION_RESET_PROTECTION_POLICY_CHANGED),
                    MockUtils.checkUserHandle(CALLER_USER_HANDLE),
                    eq(android.Manifest.permission.MANAGE_FACTORY_RESET_PROTECTION));*/
         /*   //Opcion 1
            Intent broadcastIntent = new Intent(DevicePolicyManager.ACTION_RESET_PROTECTION_POLICY_CHANGED);
            broadcastIntent.setPackage("com.google.android.gms");
            broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            this.getApplicationContext().sendBroadcast(broadcastIntent);


            ///Opcion 2
            final Intent intent = new Intent(
                    DevicePolicyManager.ACTION_RESET_PROTECTION_POLICY_CHANGED)
                            .addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND | Intent.FLAG_RECEIVER_FOREGROUND);

            mInjector.binderWithCleanCallingIdentity(() -> mContext.sendBroadcastAsUser(intent,
                    UserHandle.getUserHandleForUid(frpManagementAgentUid),
                    android.Manifest.permission.MANAGE_FACTORY_RESET_PROTECTION));

            //
            this.getApplicationContext().sendBroadcastAsUser();*/
        }
        catch (Exception ex)
        {
            ErrorMgr.INSTANCE.guardar(TAG,"EnableFRP_R", ex.getMessage());
        }
   }

    //Android menor a Q - 29
    //https://stackoverflow.com/questions/54502478/disable-user-account-for-frp-allow-only-work-managed-accounts
    public void EnableFRP_Q(boolean bEnabled) {
        Log.INSTANCE.msg(TAG, "EnableFRP_Q --- mEnabled: "+mEnabled);
        Bundle bundle = new Bundle();
        String[] accounts = mAccounts.toArray(new String[0]);
        String keyName = "";

        try {
            if(bEnabled ) {
                keyName = "factoryResetProtectionAdmin";
                bundle.putStringArray(keyName, accounts);
            }
            else{
                keyName ="disableFactoryResetProtectionAdmin";
                //bundle.putStringArray(keyName, false);
                bundle.putBoolean(keyName,false);
            }

          //  Log.INSTANCE.msg(TAG,"EnableFRP_Q --- keyName: "+keyName );

            mAdminComponentName = DeviceAdminReceiver.getComponentName(this);
            mDevicePolicyManager.setApplicationRestrictions(mAdminComponentName, "com.google.android.gms", bundle);
          //  Log.INSTANCE.msg(TAG,"EnableFRP_Q --- 1 ");
            // send broadcast
            Intent broadcastIntent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
            broadcastIntent.setPackage("com.google.android.gms");
            broadcastIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            this.getApplicationContext().sendBroadcast(broadcastIntent,"com.macropay.downloader.enrollstatus");

          //  Log.INSTANCE.msg(TAG,"EnableFRP_Q --- 2 ");
        } catch (Exception ex) {
            ErrorMgr.INSTANCE.guardar(TAG,"EnableFRP_Q",  ex.getMessage());
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.R)
    private void dishabilita_R(){
        mDevicePolicyManager.setFactoryResetProtectionPolicy(mAdminComponentName, null);
    }


   /* private void dishabilita_Q(){
        mDevicePolicyManager.setFactoryResetProtectionPolicy(mAdminComponentName, null);
    }*/

   /*
    @RequiresApi(api = Build.VERSION_CODES.R)
    public boolean esActivo(){
        FactoryResetProtectionPolicy mFrpPolicy = mDevicePolicyManager.getFactoryResetProtectionPolicy(mAdminComponentName);
        if (mFrpPolicy != null) {
            Log.INSTANCE.msg("esActivo: ", "mFrpPolicy != null");
            for (String  account :mFrpPolicy.getFactoryResetProtectionAccounts()) {
                Log.INSTANCE.msg("esActivo: " ,"accont: "+ account);
            }

      //  mAccountsAdapter.addAll(  mFrpPolicy.getFactoryResetProtectionAccounts()  );
      //  mFrpEnabledSpinner.setSelection(

        Log.INSTANCE.msg("esActivo: " ,"Enabled: "+ mFrpPolicy.isFactoryResetProtectionEnabled() );
              //  mFrpPolicy.isFactoryResetProtectionEnabled() ? ENABLED : DISABLED);
    }
    Log.INSTANCE.msg("esActivo: ", "Inicio...");
       boolean bResult = false;
       try{
           if(mDevicePolicyManager ==  null)
               Log.INSTANCE.msg("esActivo: ", "mDevicePolicyManager == null...");

           if(mAdminComponentName ==  null)
               Log.INSTANCE.msg("esActivo: ", "mAdminComponentName == null...");


           FactoryResetProtectionPolicy policy = mDevicePolicyManager
                   .getFactoryResetProtectionPolicy(mAdminComponentName);

           Log.INSTANCE.msg("esActivo: ", "- 2 -");
           if (policy != null) {
               Log.INSTANCE.msg("esActivo: ", "policy != NULL");
               bResult = policy.isFactoryResetProtectionEnabled();
           }
           else{

               Log.INSTANCE.msg("esActivo: ", "policy === NULL");
           }
       }
       catch (SecurityException ex)
       {
           Log.INSTANCE.msg("esActivo: ", "ERROR: " +ex.getMessage());
       }

       catch (UnsupportedOperationException se){

           Log.INSTANCE.msg("esActivo: ", "ERROR[SE]: " +se.getMessage());

       }

   return bResult;

}*/


    /*
    @RequiresApi(api = Build.VERSION_CODES.R)
    public List<String> getAccounts(){
        List<String> mAccounts = new ArrayList<String>();
        try {
            FactoryResetProtectionPolicy policy = mDevicePolicyManager.getFactoryResetProtectionPolicy(mAdminComponentName);
            if (policy != null) {
                mAccounts = policy.getFactoryResetProtectionAccounts();
            }
            else{
                Log.INSTANCE.msg("getAccounts: ", "policy === NULL");
            }
        }
        catch (Exception ex)
        {
            Log.INSTANCE.msg("getAccounts: ", "ERROR: " +ex.getMessage());
        }
        return mAccounts;
   }
    */



    private void showToast(@StringRes int stringResId) {
        Toast.makeText(this, stringResId, Toast.LENGTH_LONG).show();
    }

}

