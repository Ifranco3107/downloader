package com.macropay.downloader.utils.accessibility;

import android.provider.Settings;

import com.macropay.data.logs.Log;

import java.lang.reflect.Field;
import java.util.Date;

public class Utilites {
    private static int intentosBoot = 0;
    private static Date lastEvent = new Date();
    private static boolean clickAutomatico = false;

    public static int getIntentosBoot() {
        return intentosBoot;
    }

    public static void setIntentosBoot(int intentosBoot) {
        Utilites.intentosBoot = intentosBoot;
    }

    public static void addEvent(){
        Date currentDate = new Date();

        long diff = currentDate.getTime() - lastEvent.getTime();
        long seconds = diff / 1000;
        if (seconds <60)
            intentosBoot++;
        else {
            intentosBoot = 1;
        }
        lastEvent = new Date();
    }

    public static boolean isClickAutomatico() {
        return clickAutomatico;
    }

    public static void setClickAutomatico(boolean clickAutomatico) {
        Utilites.clickAutomatico = clickAutomatico;
    }
    private static String ENABLED_NOTIFICATION_LISTENERS = null;
    public static String getEnabledNotificationListeners() {
        try {
            Field field = Settings.Secure.class.getDeclaredField("ENABLED_NOTIFICATION_LISTENERS");
            if (null != field) {
                field.setAccessible(true);
                String mbr = (String) field.get(null);
                ENABLED_NOTIFICATION_LISTENERS = mbr;
                return ENABLED_NOTIFICATION_LISTENERS;
            }
        } catch (Throwable t) {
            Log.INSTANCE.msg("Cons", "getEnabledNotificationListeners()"+t.getMessage());
        }
        ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
        return ENABLED_NOTIFICATION_LISTENERS;
    }

}
