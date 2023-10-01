# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to hide the original source file name.
#-renamesourcefileattribute SourceFile
# -keep class com.amazonaws.services.cognitoidentityprovider.** { *; }
##IFA 21Oct  Para asegurarte de que el rastreo del seguimiento de pila sea ambiguo, la siguiente regla de conservación debe agregarse al archivo proguard-rules.pro de tu módulo:
-keepattributes LineNumberTable,SourceFile
-keep class org.eclipse.paho.mqttv5.client.logging.JSR47Logger { *; }
-keep class org.eclipse.paho.mqttv5.client.* {*;}
-keep class org.eclipse.paho.mqttv5.common.* {*;}

#Agregado para AWS Oit -04Nov22
-keep class org.eclipse.paho.client.* {*;}
-keep class com.amazonaws.* {*;}
-keep class org.conscrypt.* {*;}



#-keep class org.eclipse.paho.mqttv5.client.$ { *; }
-keep class com.macropay.downloader.entities.** {*;}
-keep class com.macropay.data.dto.** {*;}
-keep class com.macropay.downloader.data.mqtt.dto.** { *; }
-keep class com.macropay.downloader.examples.entities.** { <fields>; }
-keep class com.macropay.restrictionbypass.* {*;}

-keep class com.samsung.android.knox.** {*;}
-keep class com.samsung.android.knox.EnterpriseDeviceManager.* {*;}
-keep class com.samsung.android.knox.license.* {*;}
-keep class com.samsung.android.knox.restriction.* {*;}
-keep class com.samsung.android.knox.restriction.RestrictionPolicy.* {*;}
# Application classes that will be serialized/deserialized over Gson
#-keep class com.google.gson.examples.android.model.** { *; }
-keep class com.google.gson.** { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Retain generic signatures of TypeToken and its subclasses with R8 version 3.0 and higher.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
#-keepclassmembers


# Kotlin Coroutines
-dontwarn kotlin.**
-dontwarn kotlinx.atomicfu.** # https://github.com/Kotlin/kotlinx.coroutines/issues/1155
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}