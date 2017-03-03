package org.keynote.godtools.android.newnew.preferences;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.keynote.godtools.android.R;

public class NotificationPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "NotificationPref";


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.notification_preferences);
    }
}
