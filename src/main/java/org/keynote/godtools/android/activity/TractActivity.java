package org.keynote.godtools.android.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.tract.activity.BaseTractActivity;

import java.util.Locale;

public class TractActivity extends BaseTractActivity {
    public static void start(@NonNull final Context context, final long toolId, @NonNull final Locale primary,
                             @Nullable final Locale parallel) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolId, primary, parallel);
        context.startActivity(new Intent(context, TractActivity.class).putExtras(extras));
    }
}
