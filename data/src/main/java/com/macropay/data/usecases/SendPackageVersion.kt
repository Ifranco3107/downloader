package com.macropay.data.usecases

import android.content.Context
import com.google.gson.Gson
import com.macropay.data.di.Session
import com.macropay.data.di.UserSessionCredentials
import com.macropay.data.dto.request.PackageVersionDto
import com.macropay.data.preferences.Values
import com.macropay.data.repositories.PackageVersionRepository
import com.macropay.data.dto.request.PackageFile
import com.macropay.data.dto.request.toApp
import com.macropay.data.repositories.TrxOffline
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.phone.DeviceCfg
import com.macropay.utils.phone.DeviceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


class SendPackageVersion
@Inject constructor(
    @ApplicationContext val context: Context
) {
    private val TAG = "SendPackageVersion"

    @Inject
    lateinit var packageVersionRepository: PackageVersionRepository


    @Inject
    lateinit var cabeceras: UserSessionCredentials

    fun send(lstApps: MutableList<PackageFile>) {


        Log.msg(TAG, "[send]")
        var imei = DeviceInfo.getDeviceID() //  DeviceCfg.getImei(context)
        val packageVersionDto = PackageVersionDto(
            apps = lstApps.map { it.toApp() },
            imei
        )
        val gson = Gson()
        val data = gson.toJson(packageVersionDto)
        Log.msg(TAG, "[send] data: \n" + data);

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                try {

                    val response = packageVersionRepository.execute(packageVersionDto, cabeceras)

                            if (response.isSuccessful) {
                                Log.msg(TAG, "packageVersionRepository = isSuccessful")
                            } else {
                                Log.msg(TAG, "packageVersionRepository = isSuccessful")
                            }
                } catch (ex: Exception) {
                    TrxOffline.guardarDtoTrx("PKG", packageVersionDto)
                    val json = Gson().toJson(packageVersionDto)
                    ErrorMgr.guardar(TAG, "send", ex.message, json)

                }
            }

        }


    }

}