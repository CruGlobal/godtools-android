package org.cru.godtools.tract.analytics.model;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.cru.godtools.analytics.model.AnalyticsActionEvent;
import org.cru.godtools.analytics.model.AnalyticsSystem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.cru.godtools.analytics.AdobeAnalyticsService.KEY_CONTENT_LANGUAGE_SECONDARY;

public final class ToggleLanguageAnalyticsActionEvent extends AnalyticsActionEvent {
    private static final String ACTION_TOGGLE_LANGUAGE = "Parallel Language Toggled";
    private static final String KEY_ADOBE_TOGGLE_LANGUAGE = "cru.parallellanguagetoggle";

    private final String mTract;
    private final Map<String, ?> mAttrs;

    public ToggleLanguageAnalyticsActionEvent(@Nullable final String tract, @NonNull final Locale locale) {
        super(null, ACTION_TOGGLE_LANGUAGE);
        mTract = tract;
        final HashMap<String, Object> attrs = new HashMap<>();
        attrs.put(KEY_ADOBE_TOGGLE_LANGUAGE, 1);
        attrs.put(KEY_CONTENT_LANGUAGE_SECONDARY, LocaleCompat.toLanguageTag(locale));
        mAttrs = Collections.unmodifiableMap(attrs);
    }

    @Override
    public boolean isForSystem(@NonNull final AnalyticsSystem system) {
        return system == AnalyticsSystem.ADOBE || system == AnalyticsSystem.FACEBOOK;
    }

    @Nullable
    @Override
    public Map<String, ?> getAttributes() {
        return mAttrs;
    }

    @Nullable
    @Override
    public String getAdobeSiteSection() {
        return mTract;
    }
}
