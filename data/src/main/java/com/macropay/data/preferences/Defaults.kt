package com.macropay.data.preferences

import com.macropay.data.BuildConfig
import com.macropay.utils.preferences.Cons

//Aqui solo van los valores por default,
object Defaults {
    const val KEY_PREENROLAMIENTO = "device_auth_status"

    /*
    //==========================================
    //     Produccion
    //========================================== */



    //Enlaces de respaldo para cada ambiente
    const val SERVIDOR_HTTP2_DEV = "https://lapimacrolockdev.lockmacropay.mx"
    const val SERVIDOR_HTTP2_QA = "https://lapimacrolockqa.lockmacropay.mx"
    const val SERVIDOR_HTTP2_PROD = "https://lapimacrolock.lockmacropay.mx"
    const val SERVIDOR_HTTP =  SERVIDOR_HTTP2_PROD //"https://5wu9janfie.execute-api.us-east-1.amazonaws.com/"

    //Enlaces actuales para cada ambiente
    const val SERVIDOR_HTTP_CURRENT_DEV = "https://2nnshbeuo1.execute-api.us-east-1.amazonaws.com/"
    const val SERVIDOR_HTTP_CURRENT_QA = "https://4br90mfkwa.execute-api.us-east-1.amazonaws.com/"
    const val SERVIDOR_HTTP_CURRENT_PROD = "https://5wu9janfie.execute-api.us-east-1.amazonaws.com/"
    const val SERVIDOR_HTTP_PKG = "https://apackagemacrolockdev.macropay.mx/"
    const val SERVIDOR_HTTP_RPT = "https://gq4m9d4dfc.execute-api.us-east-1.amazonaws.com/"
    const val SERVIDOR_FILE = "https://4br90mfkwa.execute-api.us-east-1.amazonaws.com/"
    const val SERVIDOR_MQTT = "a3fu00idp2mj0v-ats.iot.us-east-1.amazonaws.com"
    const val APP_BUSSINES_PACKAGE = "com.grupomacro.macropay"
    const val ENROLL_SOURCE = "zerotouch"
//    const val API_KEY: String = "CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s"
    const val API_KEY: String = BuildConfig.API_KEY


    //http://45.190.236.239:8888/lock/api/download/grupomacroapp.apk
    const val APP_BUSSINES_LOCATION = "/lock/api/download/grupomacroapp.apk"
    const val APP_MANUAL_PACKAGE = "com.macropay.macropaguitos"


    const val EULA_TITLE = "Contrato"
    const val EULA_DEFAULT = "What is Lorem Ipsum?\n" +
            "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.\n" +
            "\n" +
            "Why do we use it?\n" +
            "It is a long established fact that a reader will be distracted by the readable content of a page when looking at its layout. The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text, and a search for 'lorem ipsum' will uncover many web sites still in their infancy. Various versions have evolved over the years, sometimes by accident, sometimes on purpose (injected humour and the like).\n" +
            "\n" +
            "\n" +
            "Where does it come from?\n" +
            "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of \"de Finibus Bonorum et Malorum\" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, \"Lorem ipsum dolor sit amet..\", comes from a line in section 1.10.32.\n" +
            "\n" +
            "The standard chunk of Lorem Ipsum used since the 1500s is reproduced below for those interested. Sections 1.10.32 and 1.10.33 from \"de Finibus Bonorum et Malorum\" by Cicero are also reproduced in their exact original form, accompanied by English versions from the 1914 translation by H. Rackham."
    const val BIENVENIDA_TITLE = "!!Bienvenido a MacroPay !!!"
    const val BIENVENIDA_BODY = "Recuerda que puedes pagar, ganar y participar \nen programas de lealtad desde tu app"

    const val TEXT_BLOQUEO_DEFAULT = "Hola **@nombreCliente**, detectamos que tu equipo fue bloqueado por falta de pago.\n" +
            "No pierdas la comunicación,\n" +
            "puedes pagar:\n" +
            "Desde la APP en la opción [Paga en Línea]\n" +
            "en nuestras tiendas Macropay\n" +
            "en la tienda de tu preferencia\n" +
            "y sigue disfrutando de los beneficios de tu línea telefónica.\n" +
            "¡Te esperamos!"
    var apps =
        "[\"com.whatsapp\",\"com.facebook.katana\",\"com.facebook.lite\",\"com.facebook.orca\",\"com.facebook.mlite\",\"com.whatsapp.w4b\",\"org.telegram.messenger\",\"com.google.android.apps.maps\",\"com.waze\",\"com.ubercab\",\"com.didiglobal.passenger\",\"com.spotify.music\",\"com.google.android.youtube\",\"com.android.vending\",\"com.snapchat.android\",\"com.google.android.gm\",\"com.instagram.android\",\"mx.com.bancoazteca.bazdigitalmovil\",\"com.google.android.googlequicksearchbox\",\"com.bancomer.mbanking\",\"com.citibanamex.banamexmobile\",\"com.android.chrome\",\"org.mozilla.firefox\",\"us.zoom.videomeetings\",\"com.google.android.apps.meetings\",\"com.zhiliaoapp.musically\",\"com.payclip.clip\",\"mx.hsbc.hsbcmexico\",\"com.linkedin.android\",\"mx.com.miapp\",\"com.motorola.camera2\",\"com.didiglobal.driver\",\"com.google.android.apps.tachyon\",\"com.google.android.apps.messaging\",\"com.netflix.mediaclient\",\"com.ubercab.driver\",\"com.ubercab.eats\",\"com.google.android.apps.youtube.music\",\"com.google.android.apps.subscriptions.red\",\"com.microsoft.office.outlook\",\"com.android.camera2\",\"com.ume.browser.cust\"]\n"


    const val EMERGENCY_NUMBER = "911"
    const val CALLCENTER_NUMBER = "8006276729"


    const val DPC_PACKAGENAME = "com.macropay.dpcmacro"
    const val DPC_LOCATION  = "https://amacrolockbucketdev.lockmacropay.mx/api/devices/mobile/apk/download?appkeymobile=CMaz3r2r23r23r23r23ssd11SWSZXWEFWWFd99s"

}