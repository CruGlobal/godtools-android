package org.keynote.godtools.android.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

import java.util.List;

public class Device
{


    public static boolean isConnected(Context context)
    {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] allNetworks = cm.getAllNetworkInfo();

        for (NetworkInfo networkInfo : allNetworks)
        {
            if (networkInfo.isAvailable() && networkInfo.isConnected())
            {
                return true;
            }

        }

        return false;
    }

    public static String getDefaultLanguage(SnuffyApplication application)
    {
        return application.getDeviceLocale().getLanguage();
    }

    public static boolean isAppInForeground(Context context)
    {
        ActivityManager manager = (ActivityManager) context.getApplicationContext().
                getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = manager.getRunningTasks(1);

        return (services.get(0).topActivity.getPackageName().equalsIgnoreCase(
                context.getApplicationContext().getPackageName()));
    }
}
