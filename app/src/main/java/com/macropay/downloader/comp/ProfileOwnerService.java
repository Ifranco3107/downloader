/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.macropay.downloader.comp;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;

import com.macropay.downloader.ui.provisioning.EnrollActivity;
import com.macropay.utils.logs.Log;

import androidx.annotation.Nullable;


/**
 * Handle cross user call from a DPC instance in other side.
 * @see {@link DevicePolicyManager#bindDeviceAdminServiceAsUser(
 * ComponentName, Intent, ServiceConnection, int, UserHandle)}
 */
public class ProfileOwnerService extends Service {
    private Binder mBinder;
    private static final String TAG = "ProfileOwnerService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.INSTANCE.msg(TAG,"[onCreate]");
        mBinder = new ProfileOwnerServiceImpl(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    static class ProfileOwnerServiceImpl extends IProfileOwnerService.Stub {
        private Context mContext;
        private DevicePolicyManager mDpm;
        private PackageManager mPm;

        public ProfileOwnerServiceImpl(Context context) {
            Log.INSTANCE.msg(TAG,"[ProfileOwnerServiceImpl]");
            mContext = context;
            mDpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            mPm = context.getPackageManager();
        }

        @Override
        public void setLauncherIconHidden(boolean hidden) throws RemoteException {
            Log.INSTANCE.msg(TAG,"[setLauncherIconHidden]");
       mPm.setComponentEnabledSetting(
                    new ComponentName(mContext, EnrollActivity.class),
                    hidden ? COMPONENT_ENABLED_STATE_DISABLED : COMPONENT_ENABLED_STATE_DEFAULT,
                    DONT_KILL_APP);
        }

        @Override
        public boolean isLauncherIconHidden() throws RemoteException {
            Log.INSTANCE.msg(TAG,"[setLauncherIconHidden]");
            return mPm.getComponentEnabledSetting(
                    new ComponentName(mContext, EnrollActivity.class))
                    == COMPONENT_ENABLED_STATE_DISABLED;
        }

        @Override
        public boolean installCaCertificate(AssetFileDescriptor afd) {
            return false;
            /*try (FileInputStream fis = afd.createInputStream()) {
                return Util.installCaCertificate(fis, mDpm, DeviceAdminReceiver.getComponentName(
                        mContext));
            } catch (IOException e) {
                Log.e(TAG, "Unable to install a certificate", e);
                return false;
            }*/
        }
    }
}
