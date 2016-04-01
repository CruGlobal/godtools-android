package org.keynote.godtools.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.keynote.godtools.android.snuffy.SnuffyApplication;

public class Device {



    public static boolean isConnected(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] allNetworks = cm.getAllNetworkInfo();

        for (NetworkInfo networkInfo : allNetworks) {
            if (networkInfo.isAvailable() && networkInfo.isConnected()) {
                return true;
            }

        }

        return false;
    }
}
