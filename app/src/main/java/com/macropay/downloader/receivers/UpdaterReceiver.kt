package com.macropay.downloader.receivers

import com.macropay.data.logs.Log.msg
import com.macropay.data.logs.ErrorMgr.guardar
import android.content.Context
import android.content.Intent
import com.macropay.utils.broadcast.Sender
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception

//class UpdaterReceiver : BroadcastReceiver() {
@AndroidEntryPoint
class UpdaterReceiver : HiltBroadcasterReceiver() {
    private var mContext: Context? = null
    var TAG = "UpdaterReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        mContext = context
        val action = intent.action
        msg(TAG, "action: $action")
        when (action) {
            Sender.ACTION_UPDATER_STATUS -> try {
                val msg = intent.getStringExtra("msg")
                msg(TAG, "ACTION_UPDATER_STATUS: $msg")
                //if(msg.equals("Descargo"))
                if (msg == "instalado") {
                    //sendDPCVersion(context);
                }
            } catch (ex: Exception) {
                guardar(TAG, "ACTION_UPDATER_STATUS", ex.message)
            }
            else -> msg(TAG, "DEFAULT")
        }
    } /*private void sendDPCVersion(Context context){
        try{
           String packageName =context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            Long versionCode = packageInfo.getLongVersionCode();
            String  versionName = packageInfo.versionName;
            ServerHTTP serverHTTP = new ServerHTTP(context);
            serverHTTP.sendPackageUpdate(context,packageName,versionCode.toString(),versionName,1);
        } catch (Exception ex) {
            ErrorMgr.guardar(TAG, "sendDPCVersion", ex.getMessage());
        }
    }*/
}