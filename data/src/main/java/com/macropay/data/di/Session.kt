package com.macropay.data.di


import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import com.macropay.data.BuildConfig
import com.macropay.data.dto.response.SyncReportResponse
import com.macropay.data.preferences.Defaults
import com.macropay.utils.Settings
import com.macropay.utils.Utils
import com.macropay.data.logs.ErrorMgr
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.Exception
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class Session @Inject constructor() {
    /**
     * Funcion para ubtener las credenciales de cognito
     */
    private val TAG = "Session"
    private var CADUCIDAD_TOKEN = 50 // en minutos 3_000

    @Inject
    lateinit var cognitoUserPool: CognitoUserPool
    var userName =  BuildConfig.pr_usernameCognito
    var clave   =BuildConfig.pr_passwordCognito
    var intento=0

    fun defEnvironment(){
        val httpServer =  Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)
        // Prod https://5wu9janfie.execute-api.us-east-1.amazonaws.com
        // Dev  https://2nnshbeuo1.execute-api.us-east-1.amazonaws.com
        // QA   https://4br90mfkwa.execute-api.us-east-1.amazonaws.com

        if(httpServer.contains(Cons.KEY_ENVIRONMENT_DV)){
            userName =  BuildConfig.dv_usernameCognito
            clave   =BuildConfig.dv_passwordCognito
        }
        if(httpServer.contains(Cons.KEY_ENVIRONMENT_QA)){
            userName =  BuildConfig.qa_usernameCognito
            clave   =BuildConfig.qa_passwordCognito
        }
    }
     fun getCredenctials(callback: (UserSessionCredentials?) -> Unit) {
        try {
            defEnvironment()
            val localCredentials = getLocalCredentials()
            if (localCredentials != null) {
                callback(localCredentials)
            } else {
                Log.msg(TAG,"[getCredenctials] Refresca las credenciales.")
                val cognitoUser = cognitoUserPool.getUser(userName)

                val authHandler = object : AuthenticationHandler {
                    override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                        //Log.d(TAG, "onSuccess: ${userSession?.idToken?.jwtToken}")
                        try{
                            val userSessionCredentials = UserSessionCredentials(
                                userSession?.username,
                                userSession?.idToken?.jwtToken,
                                userSession?.accessToken?.jwtToken,
                                userSession?.refreshToken?.token
                            )

                            saveCredentials(userSessionCredentials)

                            callback(userSessionCredentials)
                        }catch (ex:Exception ){
                            ErrorMgr.guardar(TAG,"AuthenticationHandler.onSucess",ex.message)
                        }
                    }

                    override fun getAuthenticationDetails(
                        authenticationContinuation: AuthenticationContinuation?,
                        userId: String?
                    ) {
                        try {
                            val authDetail = AuthenticationDetails(
                                userId,
                                clave,
                                null
                            )

                            authenticationContinuation?.setAuthenticationDetails(authDetail)
                            authenticationContinuation?.continueTask()
                        }catch (ex:Exception ){
                            ErrorMgr.guardar(TAG,"getAuthenticationDetails",ex.message)
                        }
                    }
                    override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                        continuation?.continueTask()
                    }

                    override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                        continuation?.continueTask()
                    }

                    override fun onFailure(exception: Exception?) {

                        Log.d(TAG, "onFailure:[1] ${exception?.message} [$userName-$clave]")
                        callback(null)
                        //[1] cognitoUser.getSessionInBackground(authHandler)
                    }
                }
                cognitoUser.getSessionInBackground(authHandler)
            }

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"getCredenctials",ex.message)

        }


    }
    //TODO: Version 2. : Nelson:13Mar23
    suspend fun getCredentials(): UserSessionCredentials? {
        var credentials: UserSessionCredentials?
        val maxIntentos = 3
        var currentIntentos = 0
        defEnvironment()
        do {
            //incrementar los intentos
            currentIntentos++
            val res = suspendCoroutine {
                continuar -> val localCredentials = getLocalCredentials()
                if (localCredentials != null)
                    continuar.resume( localCredentials)
                else {
                    val cognitoUser = cognitoUserPool.getUser(userName)
                    val authHandler = object : AuthenticationHandler {
                        override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                            //Log.d(TAG, "onSuccess: ${userSession?.idToken?.jwtToken}")
                            val userSessionCredentials = UserSessionCredentials(
                                userSession?.username,
                                userSession?.idToken?.jwtToken,
                                userSession?.accessToken?.jwtToken,
                                userSession?.refreshToken?.token
                            )
                            saveCredentials(userSessionCredentials)
                            continuar.resume(userSessionCredentials)
                        }
                        override fun getAuthenticationDetails(
                            authenticationContinuation: AuthenticationContinuation?,
                            userId: String?
                        ) {
                            val authDetail = AuthenticationDetails(
                                userId,
                                clave,
                                null
                            )
                            authenticationContinuation?.setAuthenticationDetails(authDetail)
                            authenticationContinuation?.continueTask()
                        }
                        override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                            continuation?.continueTask()
                        }
                        override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                            continuation?.continueTask()
                        }
                        override fun onFailure(exception: Exception?) {
                            Log.d(TAG, "onFailure:[2] ${exception?.message}")
                            CoroutineScope (Dispatchers.IO).launch{
                                Log.d(TAG, "onFailure:[2] intento: $currentIntentos [$userName-$clave]")
                                delay(3_000)
                                continuar.resume(null)
                            }
                        }
                    }
                    cognitoUser.getSession(authHandler)
                }
            }
            credentials = res
        } while (res == null && currentIntentos <= maxIntentos)
        return credentials
    }

    //Version inicial...
    suspend fun getCredentialsOK(): UserSessionCredentials? {

          return suspendCoroutine { continuar ->
              defEnvironment()
              val localCredentials = getLocalCredentials()

              if (localCredentials != null)
                  continuar.resume( localCredentials)
              else {

                  val cognitoUser = cognitoUserPool.getUser(userName)

                  val authHandler = object : AuthenticationHandler {
                      override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
                          //Log.d(TAG, "onSuccess: ${userSession?.idToken?.jwtToken}")
                          val userSessionCredentials = UserSessionCredentials(
                              userSession?.username,
                              userSession?.idToken?.jwtToken,
                              userSession?.accessToken?.jwtToken,
                              userSession?.refreshToken?.token
                          )

                          saveCredentials(userSessionCredentials)
                          continuar.resume(userSessionCredentials)
                      }

                      override fun getAuthenticationDetails(
                          authenticationContinuation: AuthenticationContinuation?,
                          userId: String?
                      ) {

                          val authDetail = AuthenticationDetails(
                              userId,
                              clave,
                              null
                          )

                          authenticationContinuation?.setAuthenticationDetails(authDetail)
                          authenticationContinuation?.continueTask()
                      }

                      override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
                          Log.msg(TAG,"[onFailure] getMFACode")
                          continuation?.continueTask()
                      }

                      override fun authenticationChallenge(continuation: ChallengeContinuation?) {
                          Log.msg(TAG,"[onFailure] authenticationChallenge")
                          continuation?.continueTask()
                      }

                      override fun onFailure(exception: Exception?) {
                          Log.d(TAG, "onFailure:[3] **** ${exception?.message} [$userName-$clave]" )

                            //Nelson lo pidio- 13Mar23 comentar -->
                            continuar.resume(null)

                     /*     CoroutineScope (Dispatchers.IO).launch{
                             intento++
                             delay(3_000)
                             Log.msg(TAG,"[onFailure] intento: $intento")
                             if(intento<10)
                                 getCredentials()
                             else
                                 continuar.resume(null)
                         }*/
                      }
                  }

                  cognitoUser.getSession(authHandler)
              }

            }


    }


    //verificar si las credenciales siguen vigentes
    private fun getLocalCredentials(): UserSessionCredentials? {

        var ultimaConexion = Settings.getSetting(Cons.KEY_COGNITO_LAST_CONECTION, LocalDateTime.now())
        var tiempoTranscurrido = Utils.tiempoTranscurrido(ultimaConexion, ChronoUnit.MINUTES).toInt()

        if (tiempoTranscurrido == 0 || tiempoTranscurrido >= CADUCIDAD_TOKEN) {
            Log.msg(TAG,"[getLocalCredentials] Token caducado... tiempoTranscurrido: "+tiempoTranscurrido)
            return null
        }
        //usar local
        var username = Settings.getSetting(Cons.KEY_COGNITO_USERNAME, "")
        var idToken = Settings.getSetting(Cons.KEY_COGNITO_ID_TOKEN, "")
        var accessToken = Settings.getSetting(Cons.KEY_COGNITO_ACCESS_TOKEN, "")
        var refreshToken = Settings.getSetting(Cons.KEY_COGNITO_USERNAME, "")

        return UserSessionCredentials(username, idToken, accessToken, refreshToken)

    }

    private fun saveCredentials(cognitoUserSessionCredentials: UserSessionCredentials) {
        try {
            //actualizando fecha de ultima consulta de tokens cognito
            Settings.setSetting(Cons.KEY_COGNITO_LAST_CONECTION, LocalDateTime.now())
            Settings.setSetting(Cons.KEY_COGNITO_USERNAME, cognitoUserSessionCredentials.userName!!)
            Settings.setSetting(Cons.KEY_COGNITO_ID_TOKEN, cognitoUserSessionCredentials.idToken!!)
            Settings.setSetting(Cons.KEY_COGNITO_ACCESS_TOKEN, cognitoUserSessionCredentials.accessToken!!)
            Settings.setSetting(Cons.KEY_COGNITO_RFRESH_TOKEN, cognitoUserSessionCredentials.refreshToken!!)

        } catch (ex: Exception) {
            ErrorMgr.guardar(TAG,"saveCredentials",ex.message)
        }
    }

}