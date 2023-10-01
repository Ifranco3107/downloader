package com.macropay.downloader.utils.policies


import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*


class ResetPwd : AppCompatActivity() {
    val TAG = "ResetPwd"
    private lateinit var mDpm: DevicePolicyManager
    private lateinit var mAdminComponentName: ComponentName
    private var mKeyguardMgr: KeyguardManager? = null


    private val REQUEST_CONFIRM_CREDENTIAL = 1
    private var mContext:Context? = null
    private var mToken : String = ""

    //Registry to register for activity result
    private val mRegistry: ActivityResultRegistry? = null
    private var launcher: ActivityResultLauncher<Intent>? = null

    private fun setUpLauncher() {
        try{
        launcher = mRegistry!!.register("key",
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {

                override fun onActivityResult(result: ActivityResult?) {
                   Log.msg(TAG,"[onActivityResult] result:" +result!!.getResultCode())
                   if (result!!.getResultCode() === RESULT_OK) {
                       Log.msg(TAG,"[onActivityResult] RESULT_OK:")
                       // There are no request codes
                       val data: Intent = result!!.getData()!!
                   }
                }
            })
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"setUpLauncher",ex.message)
        }
    }
    fun changePwd(context: Context){
        attachBaseContext(context)

        mContext = context
        //
        mDpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mAdminComponentName =  DeviceAdminReceiver.getComponentName(context) as ComponentName
        mKeyguardMgr = context.getSystemService<KeyguardManager>(KeyguardManager::class.java)
        try{
            setUpLauncher()
            resetClave()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"changePwd",ex.message)
        }
    }

    private fun resetPasswordWithToken() {
        Log.msg(TAG,"[createNewPasswordToken] - mToken: "+mToken)
        val tokenString: String = mToken // "12345678"
        val token: ByteArray?
        token = try {
            Base64.getDecoder().decode(tokenString)
        } catch (e: IllegalArgumentException) {
            tokenString.toByteArray(StandardCharsets.UTF_8)
        }
  /*      var token: ByteArray? = tokenString.toByteArray(StandardCharsets.UTF_8)

        try {
            token = Base64.getDecoder().decode(tokenString)
            Log.msg(TAG,"[resetPasswordWithToken] token ok")
        } catch (e: IllegalArgumentException) {
            Log.msg(TAG,"[resetPasswordWithToken] token error")
        }*/

        try{
            val password: String ="Gis1234"
            var flags = 0
            flags = flags or if (false) DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY else 0
            flags = flags or if (false)  RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT else 0
            flags = flags or if (false) DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY else 0

            Log.msg(TAG,"[resetPasswordWithToken] flags: "+flags)
            if (token != null) {
                val result: Boolean = mDpm.resetPasswordWithToken(
                    mAdminComponentName,
                    password, token, flags
                )
                if (result) {
                    Log.msg(TAG,"[resetPasswordWithToken] OK")
                } else {
                    Log.msg(TAG,"[resetPasswordWithToken] failed")
                }
            } else {
                Log.msg(TAG,"[resetPasswordWithToken] No token")
                //showToast(getString(R.string.reset_password_no_token))
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"resetPasswordWithToken",ex.message)
        }
    }
    
    
    //--->
    //*********************************************************************************************************//
    //*********************************************************************************************************//
    fun resetClave(){
        Log.msg(TAG,"[resetClave] -1-")
        createNewPasswordToken()
        activatePasswordToken()
        resetPasswordWithToken()
    }

    private fun generateRandomPasswordToken(): ByteArray? {
        return try {
            SecureRandom.getInstance("SHA1PRNG").generateSeed(32)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    private fun createNewPasswordToken() {
        Log.msg(TAG,"[createNewPasswordToken]")
        val token = generateRandomPasswordToken()
        if (!mDpm.setResetPasswordToken(
                mAdminComponentName,
                token
            )
        ) {
            Log.msg(TAG,"[createNewPasswordToken] set_password_reset_token_failed")
            return
        }
        Log.msg(TAG,"[createNewPasswordToken] creo token correctamente")
        savePasswordResetTokenToPreference(token)
        reloadTokenInfomation()
    }


    private fun activatePasswordToken() {
        Log.msg(TAG,"[activatePasswordToken]")
        try{
            val intent = mKeyguardMgr!!.createConfirmDeviceCredentialIntent(null, null)
            if (intent != null) {
                //startActivityForResult(intent, REQUEST_CONFIRM_CREDENTIAL)
                launcher!!.launch(intent);
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"",ex.message)
        }

    }

    private fun showToast(msg: String) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show()
    }
    private fun savePasswordResetTokenToPreference(token: ByteArray?) {
        if (token != null) {
            mToken = Base64.getEncoder().encodeToString(token)
        } else {
            mToken = ""
        }
        Settings.setSetting("token",mToken)
    }

    fun loadPasswordResetTokenFromPreference(context: Context): ByteArray? {

        val tokenString = Settings.getSetting("token", "")
        return if (!tokenString.isEmpty()) {
            Base64.getDecoder().decode(tokenString.toByteArray(StandardCharsets.UTF_8))
        } else {
            null
        }
    }

    private fun reloadTokenInfomation() {
        val token: ByteArray? = loadPasswordResetTokenFromPreference(mContext!!)
        val tokenString = if (token != null)
            Base64.getEncoder().encodeToString(token)
        else
           ""
        mToken = tokenString
        val active = mDpm.isResetPasswordTokenActive(mAdminComponentName)
        Log.msg(TAG,"[reloadTokenInfomation] reset_password_token active: "+active + " mToken: "+mToken)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONFIRM_CREDENTIAL) {
            if (resultCode == RESULT_OK) {
                Log.msg(TAG,"[onActivityResult] ok")
                reloadTokenInfomation()
            } else {
                showToast(" activate_reset_password_token_cancelled")
            }
        }
    }
}