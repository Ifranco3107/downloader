package com.macropay.data.di



import android.content.Context
import com.amazonaws.ClientConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.regions.Regions
import com.macropay.data.BuildConfig
import com.macropay.data.R
import com.macropay.data.preferences.Defaults
import com.macropay.data.server.DeviceAPI
import com.macropay.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ServerModule {
//       .baseUrl(httpServer)
    @Singleton
    @Provides
    fun provideRetrofit(@ApplicationContext context: Context): Retrofit {
        Settings.init(context)
        Log.init("downloader",context)
        Log.msg("ServerModule", "[provideRetrofit] \n\n\n\n\n\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")

        var httpServer =   Defaults.SERVIDOR_HTTP
        try {
            httpServer = Settings.getSetting(Cons.KEY_HTTP_SERVER, Defaults.SERVIDOR_HTTP)
        }catch (ex:Exception){

        }
        val httpClient = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
           // .addNetworkInterceptor()
            .addInterceptor(networkInterceptor)


            .build()


/*
    val httpClient = OkHttpClient.Builder()
        .callTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(30, TimeUnit.SECONDS) //recomiendan 100
        .readTimeout(30, TimeUnit.SECONDS) //Recomiendan 100
     //   .addInterceptor(networkInterceptor)
            .build()
*/

    //Retrofit
    var builder  =   Retrofit.Builder()
            .baseUrl(httpServer)
            .addConverterFactory(GsonConverterFactory.create())
          //   .addConverterFactory(ScalarsConverterFactory.create())
            //.addConverterFactory(JacksonConverterFactory.create())
          //  .addConverterFactory(MyJsonConverter.create())
           .client(httpClient)
            .build()
    return builder

    }

 /*        return Retrofit.Builder()
            .baseUrl(httpServer)


            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }*/

    @Singleton
    @Provides
    fun provideDeviceApiClient(retrofit: Retrofit): DeviceAPI {
        return retrofit.create(DeviceAPI::class.java)
    }


    @Singleton
    @Provides
    fun provideHeaderCredentials(): UserSessionCredentials {
        return UserSessionCredentials()
    }

    /*
    @Provides
    @Singleton
    fun providesCognitoUserPool(
        @ApplicationContext context: Context
    ): CognitoUserPool{
        val httpServer =  Settings.getSetting(Cons.KEY_HTTP_SERVER,Defaults.SERVIDOR_HTTP)

        var userPoolId =  BuildConfig.pr_userPoolId
        var clientId   =BuildConfig.pr_clientId
        if(httpServer.contains(Cons.KEY_ENVIRONMENT_DV)){
            userPoolId =  BuildConfig.dv_userPoolId
            clientId   =BuildConfig.dv_clientId
        }
        if(httpServer.contains(Cons.KEY_ENVIRONMENT_QA)){
            userPoolId =  BuildConfig.qa_userPoolId
            clientId   =BuildConfig.qa_clientId
        }
     return   CognitoUserPool(
                context,
                userPoolId,
                clientId,
                null,
                ClientConfiguration(),
                Regions.US_EAST_1
                )

    }
     */


}