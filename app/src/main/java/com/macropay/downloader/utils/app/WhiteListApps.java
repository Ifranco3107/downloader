package com.macropay.downloader.utils.app;

import android.annotation.TargetApi;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.os.Build;

import java.util.Collections;
import java.util.Set;


import com.macropay.downloader.DeviceAdminReceiver;

@TargetApi(Build.VERSION_CODES.R)
public class WhiteListApps  {
    private static final String DELIMITER = "\n";

    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mAdminComponent;

    private Activity activity;

    public WhiteListApps(Activity activity) {
        this.activity = activity;
        mDevicePolicyManager = this.activity.getSystemService(DevicePolicyManager.class);
        mAdminComponent = DeviceAdminReceiver.getComponentName(this.activity);
        updateCrossProfileAppsList();

    }

    public void resetApps() {
        mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, Collections.emptySet());
        updateCrossProfileAppsList();
    }

    public void addApp(String app) {
        Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
        currentApps.add(app);
        mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, currentApps);
        updateCrossProfileAppsList();
    }

    public void removeApp(String app) {
        Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
        currentApps.remove(app);
        mDevicePolicyManager.setCrossProfilePackages(mAdminComponent, currentApps);
        updateCrossProfileAppsList();
    }

    private void updateCrossProfileAppsList(){
        Set<String> currentApps = mDevicePolicyManager.getCrossProfilePackages(mAdminComponent);
        if (currentApps.isEmpty()) {
          //  mAppsList.setText(R.string.cross_profile_apps_no_whitelisted_apps);
        } else {
          //  mAppsList.setText(String.join(DELIMITER, currentApps));
        }
    }
}
