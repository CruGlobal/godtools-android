package org.keynote.godtools.android.notifications;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.common.base.Strings;

import static org.keynote.godtools.android.utils.Constants.APP_VERSION;
import static org.keynote.godtools.android.utils.Constants.REGISTRATION_ID;

/**
 * Created by ryancarlson on 7/20/15.
 */
public class NotificationUtilities
{
    public static String getStoredRegistrationId(Context context, SharedPreferences userSettings)
    {
        String registrationId = userSettings.getString(REGISTRATION_ID, "");
        if (Strings.isNullOrEmpty(registrationId))
        {
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = userSettings.getInt(APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            return "";
        }
        return registrationId;
    }

    public static void storeRegistrationId(Context context, SharedPreferences userSettings, String registrationId)
    {
        int appVersion = getAppVersion(context);
        SharedPreferences.Editor editor = userSettings.edit();
        editor.putString(REGISTRATION_ID, registrationId);
        editor.putInt(APP_VERSION, appVersion);
        editor.apply();
    }

    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
