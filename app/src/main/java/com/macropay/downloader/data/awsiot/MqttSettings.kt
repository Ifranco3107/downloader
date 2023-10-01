package com.macropay.downloader.data.awsiot

import com.macropay.data.preferences.Defaults
import com.macropay.downloader.BuildConfig
import com.macropay.downloader.utils.Settings
import com.macropay.data.logs.Log
import com.macropay.utils.preferences.Cons


object MqttSettings {
    var topic = "sdk/test/integration/ws/reconnect"
    get() {return field}
    set(value) {field = value}

    var clientId = "persistent-client-id-1"
        get() {return field}
        set(value) {field = value}

   // var endpoint = String.format("%s.iot.%s.amazonaws.com", prefix, region)
    var endPoint = "%s.iot.%s.amazonaws.com"
        get() {
            val serverMqtt = Settings.getSetting(Cons.KEY_MQTT_SERVER,Defaults.SERVIDOR_MQTT)
            return serverMqtt
        }
        set(value) {
            Settings.setSetting(Cons.KEY_MQTT_SERVER,value)
            field =  value //String.format(value, prefix, region)
            Log.msg("MqttSettings","endPoint=["+field+"]")

        }
   // var prefixx: String = iotClient.getEndpointPrefix("iot:Data")
/*    var prefix = "iot"
        get() {return field}
        set(value) {field = value }
   // var regionx = Regions.US_EAST_1.getName()
    var region =  Regions.US_EAST_1.getName()
        get() {return field}
        set(value) {field = value }*/

    var certId =  "macrolockId"
        get() {return field}
        set(value) {field = value }

    var keyStorePath =  "./"
        get() {return field}
        set(value) {field = value }

    var ONE_TWENTY_KB = 120000
        get() {return field}
        set(value) {field = value}

    var KEYSTORE_NAME = "macrolock.bks"
        get() {return field}
        set(value) {field = value}

    var KEYSTORE_PASSWORD = BuildConfig.kspw // "Renacer2022"
        get() {return field}
        set(value) {field = value}
    var IOT_POLICY_NAME = "aws-iot-policy"
        get() {return field}
        set(value) {field = value}
    var IDENTITY_POOL_ID = "1122334455"
        get() {return field}
        set(value) {field = value}

    val DefaultCertificate ="-----BEGIN CERTIFICATE-----\n" +
            "MIIDWjCCAkKgAwIBAgIVAJFMaAtpwaGJyNk3qOJGURpAk8RdMA0GCSqGSIb3DQEB\n" +
            "CwUAME0xSzBJBgNVBAsMQkFtYXpvbiBXZWIgU2VydmljZXMgTz1BbWF6b24uY29t\n" +
            "IEluYy4gTD1TZWF0dGxlIFNUPVdhc2hpbmd0b24gQz1VUzAeFw0yMjEwMTYwMDM5\n" +
            "MjFaFw00OTEyMzEyMzU5NTlaMB4xHDAaBgNVBAMME0FXUyBJb1QgQ2VydGlmaWNh\n" +
            "dGUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDBTjaStOCfDX4KW07e\n" +
            "KBsiXUqbXqmCcgXzr729Mt3oAdytJQ0NYIzWKklQ5lxtY4eegbcaKTrvI8Spff9t\n" +
            "kXWxy30B2p6sx42wvw9dSRqLjYFgKIt34RpZkyEh/tiWqtdO6igmTIkprXiHAKPO\n" +
            "Ye6YeWV0cqUn32S4+yAcuq3XrFqdx+EB7QyOZgbOLRvPv1P6BGd4N1CnWGNjd7yh\n" +
            "oL8oEM7sDdlILoxN86zc2mXPbjmDkyhg8wjO+dcQuFzlacDtTToAMKu0y14wx1P7\n" +
            "GUX5Ndt6rynciguKdTOLldgxzlXQm9nYD/cluvDBoCLLWBRuXf/WTUHNauzEIwGy\n" +
            "HBDdAgMBAAGjYDBeMB8GA1UdIwQYMBaAFIIRZk/zEmYscEykN6vm41k81XkBMB0G\n" +
            "A1UdDgQWBBT1plKzY8unk+AK5dLtn5mXO4SRcDAMBgNVHRMBAf8EAjAAMA4GA1Ud\n" +
            "DwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAHGQKARk57loMtPK3V3Bk95Vk\n" +
            "1iJ2O42gSEHSjs5SQqkMIcHHtUODHBAlWMm+VPJ91rfQuavwhgt86xzMb31GDSuX\n" +
            "UOqxzBp+1QFT2OvHQHdWGALyudIgqg7yfn8u3U1srOLSWaFSrdNitsWz2mUds1jp\n" +
            "f9mY31NKqpmwcYqMldAj0zknpIV5v77KFs78mEBbyXRmB2o5FC+a3kZlUsCVC0Zl\n" +
            "wTrkIIPXMRcNrJJmNHav5crLsB8I5xPxwgOzmOEHFwd1ntxEFPelTjmR/j4aXXsy\n" +
            "/rTLLa4FtUA+6DVaDGdK1/QECrTGtDffVcobkqv9WItjjMjrffkITZikQQxmlg==\n" +
            "-----END CERTIFICATE-----"

    val DefaultPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n" +
            "MIIEpAIBAAKCAQEAwU42krTgnw1+CltO3igbIl1Km16pgnIF86+9vTLd6AHcrSUN\n" +
            "DWCM1ipJUOZcbWOHnoG3Gik67yPEqX3/bZF1sct9AdqerMeNsL8PXUkai42BYCiL\n" +
            "d+EaWZMhIf7YlqrXTuooJkyJKa14hwCjzmHumHlldHKlJ99kuPsgHLqt16xancfh\n" +
            "Ae0MjmYGzi0bz79T+gRneDdQp1hjY3e8oaC/KBDO7A3ZSC6MTfOs3Nplz245g5Mo\n" +
            "YPMIzvnXELhc5WnA7U06ADCrtMteMMdT+xlF+TXbeq8p3IoLinUzi5XYMc5V0JvZ\n" +
            "2A/3JbrwwaAiy1gUbl3/1k1BzWrsxCMBshwQ3QIDAQABAoIBAF0zRqCpUPue17xK\n" +
            "ldYK5DpHXKdYlsXsD+6UNxk3o0IvU6SvjoF6oazIqpy01K9xI3xK8mtMobuXNUS7\n" +
            "GSJM1cApalYwf/yyIiCrnc7OZlwOl+gy5Duibr/tbjYj122dZT4JK957XE6Z8ti0\n" +
            "eEYTdaRSM5ZXiEWXMcr2WV9AqnynJtXbywpMrOIMDFMkZjiww2cDzcb3QnB4R2m/\n" +
            "KqUCw3SGp+4Hmwxbe4ZfUVg81vLlWwWZO+BtzaY1IvM1ouqF/Wery144ZnNiExIG\n" +
            "QhGMbo3EWycIkZdO6yd2SexNr+1hLR1M9GuoR7JppiDe9t/SED5wxvA+G+kWmIWv\n" +
            "0Z7DmEECgYEA+0yM7ZeKErQWhjLpL5FVGYg6XxQzrCaH0GaJYxxHYQ2so/N7HH99\n" +
            "6IjJCzDmPp4SUXmX6AFeBGi9THsDDntR3QfdwxjcW6Rd5qkF/TaVOO9OVRd31zA7\n" +
            "SSavvK8YYVNdm0MeMPnmyH0KZIxgRZykap/P3qu9VP6LkF5JCy0x1bkCgYEAxOvv\n" +
            "zsMrEGshYk/6TIyGOi/hKj/4GCJLJ7AWzoqi4R1W5LkXZ0cv1q7UyZGGM80jNfhq\n" +
            "K0dw/vy+thrQ0aVPWwcMTnWdWlrtY+xWDe8o1xyUaNIuKbH9sG7gE1WQ9JFFIxxC\n" +
            "hNekME5cMAWR2yaPXefCeNoFpIOq7etybYeUJkUCgYEAkdj34d0VkY1blT9u/uCG\n" +
            "V28tEYmUA9HI+TbJffAnrThWfkENrph/5rmclRYoSydK0maTt18zxbNUSlMKjxkb\n" +
            "hgDOwDBpk/ZtWTW3+CH5sTWSbOhV8VpYzjfZvtSQfcv4g+gMWbhwLOQFaB3gsgYb\n" +
            "lpZx+nYKSI4DSiKumgH87mECgYAgj/celI1JcL2beSglglMrygEMsHlA3GLbFDYh\n" +
            "PzGB0g0tUyH4lP/nGw/aqPKtHC3HXeWIdI7Ny1Np33NKR3Cg18CENNSpPUHl9Q2h\n" +
            "zjtNns83qhGyGVme1Ca/a7XFi0QIc0LYnqPqzPpXgoRE3dJ1L76rpzmpo5AfOYC5\n" +
            "deLclQKBgQCZ2kynW3HqFqOiLqLE/LLtOhBpNuByXk71S6qvFH5CWL9lD35m/B+m\n" +
            "dBTJ5ib03HUF6C5LJI8Y8aWGlvaW8Sw7zn0aNe1phJklmj+CNeOdO+2qQKXN+SmG\n" +
            "R3FMiVzMIWWjZUBYvnF72NkWgfGTIvdqVrVDJ/CurioiOc49tbv0Mg==\n" +
            "-----END RSA PRIVATE KEY-----"
}
