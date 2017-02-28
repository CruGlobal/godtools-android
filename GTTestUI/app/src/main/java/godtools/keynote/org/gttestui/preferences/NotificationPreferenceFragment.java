package godtools.keynote.org.gttestui.preferences;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import godtools.keynote.org.gttestui.R;

public class NotificationPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "NotificationPref";


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.notification_preferences);
    }
}
