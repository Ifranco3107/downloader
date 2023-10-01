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
package com.macropay.downloader.ui.provisioning

import com.macropay.data.logs.Log.init
import com.macropay.data.logs.ErrorMgr.init
import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.Log.e
import com.macropay.data.logs.ErrorMgr.guardar
import android.app.Activity
import android.os.Bundle
import com.macropay.downloader.domain.usecases.provisioning.PostProvisioningTask
import android.widget.Toast
import com.macropay.downloader.R

import java.lang.Exception

class ProvisioningSuccessActivity : Activity() {
    public override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        init("downloader", this)
        init(this)
        var ln = 0
        try {
            msg(TAG, "[onCreate]")
            // Toast.makeText(this, "ProvisioningSuccessActivity", Toast.LENGTH_SHORT).show();
            val task = PostProvisioningTask(this)
            if (!task.performPostProvisioningOperations()) {
                msg(TAG, "[onCreate] finish")
                finish()
                return
            }
            ln = 1
            val launchIntent = task.getPostProvisioningLaunchIntent(intent)
            if (launchIntent != null) {
                msg(TAG, "[onCreate] va ejecutar startACtiviy con FinalizeActivity")
                ln = 2
                startActivity(launchIntent)
            } else {
                ln = 3
                e(
                    TAG, "ProvisioningSuccessActivity.onCreate() invoked, but ownership "
                            + "not assigned"
                )
                Toast.makeText(this, R.string.device_admin_receiver_failure, Toast.LENGTH_LONG)
                    .show()
            }
        } catch (ex: Exception) {
            guardar(TAG, "onCreate[$ln]", ex.message)
        }
        msg(TAG, "[onCreate] finish....")
        finish()
    }

    companion object {
        private const val TAG = "ProvisioningSuccessActivity"
    }
}