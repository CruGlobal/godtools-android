package org.cru.godtools.analytics.adobe;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.cru.godtools.analytics.AdobeAnalyticsService;

public class AdobeBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        AdobeAnalyticsService.getInstance(context).onProcessReferrer(intent);
    }
}
