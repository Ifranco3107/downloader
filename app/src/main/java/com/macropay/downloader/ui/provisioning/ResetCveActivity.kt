package com.macropay.downloader.ui.provisioning

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.macropay.downloader.DeviceAdminReceiver
import com.macropay.downloader.R
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.util.*

class ResetCveActivity : AppCompatActivity() {
    val TAG = "ResetCveActivity"
    private lateinit var mDpm: DevicePolicyManager
    private lateinit var mAdminComponentName: ComponentName
    private var mKeyguardMgr: KeyguardManager? = null


    private val REQUEST_CONFIRM_CREDENTIAL = 1
   // private var mContext:Context? = null
    private var mToken : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_cve)

        mDpm = this.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mAdminComponentName =  DeviceAdminReceiver.getComponentName(this) as ComponentName
        mKeyguardMgr = this.getSystemService<KeyguardManager>(KeyguardManager::class.java)

        changePwd()
    }


    fun changePwd(): Boolean {
        try {
            Log.msg(TAG,"[changePwd] -1-")
            createNewPasswordToken()
            Log.msg(TAG,"[changePwd] -2-")

            if( mKeyguardMgr!!.isKeyguardLocked){
                Log.msg(TAG,"[changePwd] BLOQUEADO")
                activatePasswordToken()
            }
            else{
                Log.msg(TAG,"[changePwd] DESBLOQUEADO")
                resetPasswordWithToken()
            }
        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG, "changePwd", ex.message)
            terminarDlg()
        }
        return true
    }
    private fun createNewPasswordToken() {
        Log.msg(TAG,"[createNewPasswordToken]")
        try{
            val tokenByte = generateRandomPasswordToken()
            if (!mDpm.setResetPasswordToken(
                    mAdminComponentName,
                    tokenByte
                )
            ) {
                Log.msg(TAG,"[createNewPasswordToken] set_password_reset_token_failed")
                return
            }
            Log.msg(TAG,"[createNewPasswordToken] token generado-..")
            //  savePasswordResetTokenToPreference(tokenByte)
            mToken = Base64.getEncoder().encodeToString(tokenByte)
            Log.msg(TAG,"[createNewPasswordToken] guardo: [$mToken] ")
            Settings.setSetting("token", mToken )
            Log.msg(TAG,"[createNewPasswordToken] salvo token generado-..")

            //reloadTokenInfomation()
            val active = mDpm.isResetPasswordTokenActive(mAdminComponentName)
            Log.msg(TAG,"[createNewPasswordToken] isResetPasswordTokenActive: "+active +" mToken: "+mToken)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"createNewPasswordToken",ex.message)
            terminarDlg()
        }
    }

   private fun reloadTokenInfomation() {
        var ln=0
        try{
            ln=1

            val tokenByte: ByteArray? = loadPasswordResetTokenFromPreference(this)

            ln=2
            val tokenString = if (tokenByte != null)
                Base64.getEncoder().encodeToString(tokenByte)
            else
                ""
            ln=4
            mToken = tokenString
            ln=5

            val active = mDpm.isResetPasswordTokenActive(mAdminComponentName)
            ln=6
            Log.msg(TAG,"[reloadTokenInfomation] reset_password_token active: "+active +" mToken: "+mToken)
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"reloadTokenInfomation [$ln]",ex.message)
            terminarDlg()
        }
    }
    private fun activatePasswordToken() {
        Log.msg(TAG,"[activatePasswordToken]")
        try{


            val intent = mKeyguardMgr!!.createConfirmDeviceCredentialIntent("MacroLock", "Por favor introduce tu clave...")
            if (intent != null) {
                Log.msg(TAG,"[activatePasswordToken] va lanzar CONFIRM DEVICE CREDENTIALS...")
                startActivityForResult(intent, REQUEST_CONFIRM_CREDENTIAL)
            }else
            {
                //Termina Activity
                Log.msg(TAG,"Termina Activity")
                terminarDlg()
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"",ex.message)
            terminarDlg()
        }

    }
    private fun resetPasswordWithToken() {
        Log.msg(TAG,"[createNewPasswordToken] - mToken: "+mToken)
        val tokenString: String = mToken // "12345678"
        var token: ByteArray? = tokenString.toByteArray(StandardCharsets.UTF_8)

        try {
            token = Base64.getDecoder().decode(tokenString)
            Log.msg(TAG,"[resetPasswordWithToken] token ok")
        } catch (e: IllegalArgumentException) {
            Log.msg(TAG,"[resetPasswordWithToken] token error")
        }

        try{
            val password: String ="Gissy2000"
            var flags = 0
            flags = flags or if (false) DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY else 0  //don't allow other admins to change the password again until the user has entered it.
            flags = flags or if (true) DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT else 0
            flags = flags or if (true) DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY else 0

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
            terminarDlg()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"resetPasswordWithToken",ex.message)
        }
    }
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    fun loadPasswordResetTokenFromPreference(context: Context): ByteArray? {
        Log.msg(TAG,"[loadPasswordResetTokenFromPreference]")
        try{
            val tokenString = Settings.getSetting("token", "")
            Log.msg(TAG,"[loadPasswordResetTokenFromPreference] leyo -> tokenString: $tokenString")
            return if (!tokenString.isEmpty()) {
                Base64.getDecoder().decode(tokenString.toByteArray(StandardCharsets.UTF_8))
            } else {
                null
            }
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"loadPasswordResetTokenFromPreference",ex.message)
            return null
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.msg(TAG,"[onActivityResult] requestCode: "+requestCode)
        if (requestCode == REQUEST_CONFIRM_CREDENTIAL) {
            if (resultCode == RESULT_OK) {

                Log.msg(TAG,"[onActivityResult] ok")
                reloadTokenInfomation()
                resetPasswordWithToken()
                terminarDlg()
            } else {
                Log.msg(TAG,"[onActivityResult] activate_reset_password_token_cancelled")
            }
        }
    }
    private fun generateRandomPasswordToken(): ByteArray? {
        return try {
            SecureRandom.getInstance("SHA1PRNG").generateSeed(32)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }
    private fun terminarDlg() {
        Log.msg(TAG,"[terminarDlg]")
        try{
        //Informacion envida de retorno, como resultado.
        val returnIntent = Intent()
        returnIntent.putExtra("result", 1)
        setResult(RESULT_OK, returnIntent)
        finish()
        }catch (ex:Exception){
            ErrorMgr.guardar(TAG,"terminarDlg",ex.message)
        }

    }
    private fun dis(){
        //mDpm.setTrustAgentConfiguration()
     //   mDpm.
    }
}