package org.keynote.godtools.android.newnew.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.newnew.fragments.AccessCodeDialogFragment;
import org.keynote.godtools.android.newnew.fragments.ConfirmDialogFragment;

import static org.keynote.godtools.android.utils.Constants.AUTH_DRAFT;

public class PreferenceFragment extends PreferenceFragmentCompat implements
        ConfirmDialogFragment.OnConfirmClickListener,
        AccessCodeDialogFragment.AccessCodeDialogListener {
    private static final String TAG = "NotificationPref";

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.notification_preferences);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onBindPreferences() {
        super.onBindPreferences();
       bindOnClick();
    }

    private void bindOnClick() {

        Preference notification_preference_key = findPreference("translator_preference_key");
        notification_preference_key.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean newBooleanValue = (boolean) newValue;
                if (newBooleanValue == true) {
                    showAccessCodeDialog();
                } else {
                    showExitTranslatorModeDialog();
                }
                return false;
            }
        });
    }

    private void showAccessCodeDialog() {
        FragmentManager fm = getChildFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("access_dialog");
        if (frag == null) {
            frag = new AccessCodeDialogFragment();
            frag.setCancelable(false);
            frag.show(fm, "access_dialog");
        }
    }

    private void showExitTranslatorModeDialog() {
        FragmentManager fm = getChildFragmentManager();
        DialogFragment frag = (DialogFragment) fm.findFragmentByTag("confirm_dialog");
        if (frag == null) {
            frag = ConfirmDialogFragment.newInstance(
                    getString(R.string.dialog_translator_mode_title),
                    getString(R.string.dialog_translator_mode_body),
                    getString(R.string.yes),
                    getString(R.string.no),
                    "ExitTranslatorMode"
            );
            frag.show(fm, "confirm_dialog");
        }
    }

    @Override
    public void onConfirmClick(boolean positive, String tag) {
        if (positive) {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(PreferenceFragment.this.getActivity());
            defaultSharedPreferences.edit().putBoolean("translator_preference_key", false).putString(AUTH_DRAFT, "").commit();
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.notification_preferences);
            bindOnClick();
        }
        Toast.makeText(getActivity(), "onConfirmClick " + positive + " tag " + tag, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessDialogClick(boolean success) {
        if (success) {
            getPreferenceScreen().removeAll();
            addPreferencesFromResource(R.xml.notification_preferences);
            bindOnClick();
        }
        else {
            Toast.makeText(getActivity(), "Failed.", Toast.LENGTH_SHORT).show();
        }
    }

}
