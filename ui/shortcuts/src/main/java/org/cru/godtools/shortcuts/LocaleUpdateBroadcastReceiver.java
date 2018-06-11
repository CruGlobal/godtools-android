package org.cru.godtools.shortcuts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocaleUpdateBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (Intent.ACTION_LOCALE_CHANGED.equals(intent.getAction())) {
            GodToolsShortcutManager.getInstance(context).onUpdateSystemLocale(goAsync());
        }
    }
}
