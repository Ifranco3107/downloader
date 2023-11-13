package com.macropay.utils.broadcast

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

//import com.macropay.downloader.data.preferences.Kiosko
import com.macropay.utils.logs.ErrorMgr
import com.macropay.utils.logs.Log
import com.macropay.utils.preferences.Kiosko
import java.lang.Exception


@SuppressLint("StaticFieldLeak")
object Sender {
    const val TAG = "Sender"
    var mContext: Context? = null
    const val ACTION_STATUS_ENROLL = "com.macropay.downloader.action.STATUS_ENROLL"
    const val ACTION_STATUS_CHANGE = "com.macropay.downloader.action.STATUS_CHANGE"
    const val ACTION_STATUS_LOCK = "com.macropay.downloader.action.STATUS_LOCK"
    const val ACTION_STATUS_SIM = "com.macropay.downloader.action.STATUS_SIM"
    const val ACTION_STATUS_NETWORK = "com.macropay.downloader.action.STATUS_NETWORK"
    const val ACTION_START_BLOCKED = "com.macropay.downloader.action.START_BLOCKED"
    const val ACTION_END_BLOCKED = "com.macropay.downloader.action.END_BLOCKED"
    const val ACTION_HTTP_ERROR = "com.macropay.downloader.action.HTTP_ERROR"
    val ACTION_STATUS_ENROLLMENT = "com.macropay.downloader.action.STATUS_ENROLLMENT"

    //Mensajes enviades desde Macrolock
    const val ACTION_START_UPDATER = "com.macropay.dpcmacro.action.START_UPDATER"
    const val ACTION_END_UPDATER = "com.macropay.dpcmacro.action.END_UPDATER"
    const val ACTION_TEST = "com.macropay.downloader.action.TEST"


    const val ACTION_UPDATER_STATUS = "com.macropay.macropaguitos.action.STATUS"
    const val ACTION_REMOTE_COMMAND = "com.macropay.macropaguitos.action.REMOTE_COMMAND"
    const val ACTION_DPC_STATUS = "com.macropay.downloader.action.STATUS"
    const val ACTION_INSTALL_STATUS = "com.macropay.downloader.action.INSTALL"
    const val ACTION_UNINSTALL_STATUS = "com.macropay.downloader.action.UNINSTALL"
    const val ACTION_DOWNLOAD_STATUS = "com.macropay.downloader.action.DOWNLOAD"
    const val ACTION_CHANGE_SETTING = "com.macropay.downloader.action.CHANGE_SETTING"

    var ctx: Context? = null
        get() {
            return field
        }
        set(value) {
            field = value
        }

    //Mensajes enviados a el MainActivity, son para mostrar el status del proceso de instalacion.

    fun sendStatus(msg: String?) {
        try {
            Log.msg(TAG, "[sendStatus] sin ctx - msg [" + msg + "]");



            val intent = Intent(ACTION_STATUS_CHANGE)
            //intent.setPackage(ctx!!.packageName)
            intent.putExtra("msg", msg)
            if (ctx == null) {
                Log.msg(TAG, "dpcValues.getMContext() == null")
            }
            ctx!!.getApplicationContext().sendBroadcast(intent, "com.macropay.downloader.enrollstatus")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendStatus", ex.message)
        }
    }

    fun sendStatus(msg: String?, context: Context) {
        try {
            Log.msg(TAG, "[sendStatus] [" + msg + "]");
            val intent = Intent(ACTION_STATUS_CHANGE)
            intent.setPackage(ctx!!.packageName)
            intent.putExtra("msg", msg)
            if (context != null) {
                context.sendBroadcast(intent, "com.macropay.downloader.enrollstatus")
            } else {
                Log.msg(TAG, "context == null")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendStatus", ex.message)
        }
    }

    fun sendEnrollStatus(process: String, code: Int, msg: String?, id: Long, context: Context) {
        try {
            Log.msg(TAG, "[sendEnrollStatus] $process - $code  [$msg] id: [$id]");
            val intent = Intent(ACTION_STATUS_ENROLL)
            //intent.setPackage(ctx!!.packageName)
            intent.putExtra("process", process)
            intent.putExtra("code", code)
            intent.putExtra("msg", msg)
            intent.putExtra("id", id)
            if (context != null) {
                context.sendBroadcast(intent, "com.macropay.downloader.enrollstatus")
            } else {
                Log.msg(TAG, "context == null")
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendEnrollStatus", ex.message)
        }
    }

    //Este mensaje lo recibe el receiver que esta en LockedActivity para que se retire el Lock.
    fun sendLockStatus(msg: String?) {
        try {
            val intent = Intent(ACTION_STATUS_LOCK)
            // intent.setPackage(ctx!!.packageName)
            intent.putExtra("msg", msg)
            ctx!!.getApplicationContext().sendBroadcast(intent, "com.macropay.downloader.lockstatus")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendLockStatus", ex.message)
        }
    }

    fun sendDownloadStatus(id: Long?, fileDownloaded: String?, status: String?, razon: String?, razonid: Int) {
        try {
            val intent = Intent(ACTION_DOWNLOAD_STATUS)
            intent.setPackage(ctx!!.packageName)
            intent.putExtra("id", id)
            intent.putExtra("id", id)
            intent.putExtra("fileDownloaded", fileDownloaded)
            intent.putExtra("status", status)
            intent.putExtra("razon", razon)
            intent.putExtra("razonid", razonid)
            ctx!!.getApplicationContext().sendBroadcast(intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendLockStatus", ex.message)
        }
    }

    fun sendStatusUpdate(status: Int) {
        var ln = 0
        Log.msg(TAG, "[sendStatusUpdate] status: $status")
        try {
            ln = 1
            // DevicePolicyManager dpm = (DevicePolicyManager) MainApp.getMainCtx().getApplicationContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            ln = 2
            // DownloadManager   mDownloadManager = (DownloadManager) MainApp.getMainCtx().getSystemService(Context.DOWNLOAD_SERVICE);
            val intent: Intent
            if (status == 0) {
                intent = Intent(ACTION_START_UPDATER)
                Log.msg(TAG, "[sendStatusUpdate]status: " + ACTION_START_UPDATER)
            } else {
                intent = Intent(ACTION_END_UPDATER)
                Log.msg(TAG, "[sendStatusUpdate]status: " + ACTION_END_UPDATER)
            }
            ln = 3
            intent.setPackage(ctx!!.packageName)
            intent.putExtra("status", status)
            ln = 4
            ctx!!.getApplicationContext().sendBroadcast(intent)
            ln = 5
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendStatusUpdate [$ln]", ex.message)
        }
    }

    //
    fun sendBloqueo(bBlocked: Boolean, context: Context, tipoKiosko: Kiosko.eTipo) {
        //fun sendBloqueo(bBlocked: Boolean, context: Context, tipoKiosko: String) {
        var ln = 0
        Log.msg(TAG, "[sendBloqueo] ***************************************** 20Ene2023")
        Log.msg(TAG, "[sendBloqueo] bBlocked: [$bBlocked] tipoKiosko: [$tipoKiosko]")
        Log.msg(TAG, "[sendBloqueo] *****************************************")
        try {
            ln = 1
            val intent: Intent
            if (bBlocked) {
                intent = Intent(ACTION_START_BLOCKED)
                Log.msg(TAG, "[sendBloqueo] bBlocked: " + ACTION_START_BLOCKED)
            } else {
                intent = Intent(ACTION_END_BLOCKED)
                Log.msg(TAG, "[sendBloqueo] bBlocked: " + ACTION_END_BLOCKED)
            }
            ln = 3
            //intent.setPackage(context.packageName)
            intent.putExtra("Blocked", bBlocked)
            intent.putExtra("nivel", tipoKiosko.name)
            ln = 4
            //  Log.msg(TAG,"[sendBloqueo] sendBroadcast(intent)  SIN APLICATIONCONTEXT --->")
            //  ctx!!.getApplicationContext().sendBroadcast(intent)
            //name_permissions_lock_status
            ctx!!.applicationContext.sendBroadcast(intent, "com.macropay.downloader.lockstatus")
            ln = 5
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendBloqueoAutomatico [$ln]", ex.message)
        }
    }

    //Este mensaje lo recibe LockedActivity.
    fun sendSIMStatus(msg: String) {
        Log.msg(TAG, "[sendSIMStatus]: .-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.-.")
        Log.msg(TAG, "[sendSIMStatus]: avisa al LockedActivity: status: [$msg]")
        try {
            val intent = Intent(ACTION_STATUS_SIM)
            //  intent.setPackage(ctx!!.packageName)
            intent.putExtra("msg", msg)
            ctx!!.getApplicationContext().sendBroadcast(intent, "com.macropay.downloader.lockstatus")
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendSIMStatus", ex.message)
        }
    }

    fun sendNetworkStatus(bEnabled: Boolean, context: Context) {
        var ln = 0
        Log.msg(TAG, "[sendNetworkStatus] bBlocked: $bEnabled context.packageName: " + context.packageName)
        try {
            val intent: Intent
            intent = Intent(ACTION_STATUS_NETWORK)
            //  intent.setPackage(context.packageName)
            intent.putExtra("enabled", bEnabled)
            //"com.macropay.downloader.lockstatus"
            // context.applicationContext.sendBroadcast(intent,"com.macropay.downloader.lockstatus")
            context.sendBroadcast(intent, "com.macropay.downloader.lockstatus")
            ln = 5
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendNetworkStatus [$ln]", ex.message)
        }
    }

    /*
		Intent edcIntent = new Intent();
		edcIntent.setAction(EdcConnectionIntents.STOP_SERVICE_INTENT);
		LocalSendBroadcast.sendBroadcast(startingActivityContext, edcIntent);

 */
    fun sendHttpError(errorCode: Int, error: String, url: String, segs: Int, context: Context) {
        var ln = 0
        Log.msg(TAG, "[sendHttpError] *****************************************")
        Log.msg(TAG, "[sendHttpError] errorCode: [$errorCode] error: [$error] url:[$url]")
        Log.msg(TAG, "[sendHttpError] *****************************************")
        try {
            ln = 1
            val intent: Intent = Intent(ACTION_HTTP_ERROR)

            ln = 3
            //  intent.setPackage(context.packageName)
            intent.putExtra("errorCode", errorCode)
            intent.putExtra("error", error)
            intent.putExtra("url", url)
            intent.putExtra("segs", segs)
            ln = 4
            //ctx!!.applicationContext.sendBroadcast(intent,"")
            ctx!!.applicationContext.sendBroadcast(intent)
            //  LocalSendBroadcast.sendBroadcast(ctx!!.applicationContext, intent)
            // LocalBroadcastManager.getInstance(ctx!!.applicationContext).sendBroadcast(intent)


            ln = 5
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendHttpError [$ln]", ex.message)
        }
    }


    fun sendChangeSetting(tipoBloqueo: String, bEnabled: Boolean) {
        var ln = 0
/*        Log.msg(TAG, "[sendChangeSetting] *****************************************")
        Log.msg(TAG, "[sendChangeSetting] tipoBloqueo: [$tipoBloqueo] bEnabled: [$bEnabled]")
        Log.msg(TAG, "[sendChangeSetting] *****************************************")*/
        try {
            ln = 1
            val intent: Intent = Intent(ACTION_CHANGE_SETTING)

            ln = 3
            //  intent.setPackage(ctx!!.applicationContext.packageName)
            intent.putExtra("tipoBloqueo", tipoBloqueo)
            intent.putExtra("bEnabled", bEnabled)
            ln = 4
            ctx!!.applicationContext.sendBroadcast(intent, "com.macropay.downloader.lockstatus")
            ln = 5
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendChangeSetting [$ln]", ex.message)
        }
    }

    fun sendRemoteCommand(context: Context, idCommand: Int, param1: String?, param2: String?) {
        Log.msg(TAG, "sendRemoteCommand: $idCommand")
        try {
            val intent = Intent(ACTION_REMOTE_COMMAND)
            intent.setPackage(context.packageName)
            intent.putExtra("idCommand", idCommand)
            intent.putExtra("param1", param1)
            intent.putExtra("param1", param1)
            context.applicationContext.sendBroadcast(intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendRemoteCommand", ex.message)
        }
    }

    fun sendEnrollProcess(context: Context, success: Boolean, code: Int, conbody: String) {
        Log.msg(TAG, "[sendEnrollStatus]")
        try {
            val intent: Intent = Intent(ACTION_STATUS_ENROLLMENT)
            intent.setPackage(context.applicationContext.getPackageName())
            intent.putExtra("success", success)
            intent.putExtra("code", code)
            // intent.putExtra("body", body)
            context.applicationContext.sendBroadcast(intent)
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "sendEnrollStatus", ex.message)
        }
    }
}