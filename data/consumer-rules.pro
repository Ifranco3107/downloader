#Edwin
#Retrofit v3
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepclassmembernames interface * {
    @retrofit2.http.* <methods>;
}
# GSON Annotations
-keepclassmembers class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class com.macropay.downloader.data.mqtt.dto.** { *; }
-keepclassmembers class com.macropay.data.dto.** { *; }
-keepclassmembers class com.macropay.data.mapper.** { *; }
-keepclassmembers class com.macropay.data.model.** { *; }
# -keep class com.amazonaws.services.cognitoidentityprovider.** { *; }
#-keep class retrofit2.Response { *; }
#-keepclassmembers class com.macropay.data.repositories.** {*;}
#-keep class com.macropay.data.repositories.EnrollRepository
#Retrofit v3