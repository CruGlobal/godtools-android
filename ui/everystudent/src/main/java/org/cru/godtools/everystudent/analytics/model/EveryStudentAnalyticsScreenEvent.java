package org.cru.godtools.everystudent.analytics.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.cru.godtools.analytics.model.AnalyticsScreenEvent;

import java.util.Locale;

public class EveryStudentAnalyticsScreenEvent extends AnalyticsScreenEvent {
    public static final String SCREEN_EVERYSTUDENT = "EveryStudent";
    private static final String SITE_SUB_SECTION_EVERYSTUDENT = "everystudent";
    private static final String SITE_SECTION_ARTICLES = "articles";

    public EveryStudentAnalyticsScreenEvent(@NonNull final String screen) {
        super(screen, Locale.ENGLISH);
    }

    @Nullable
    @Override
    public String getAdobeSiteSection() {
        return SITE_SECTION_ARTICLES;
    }

    @Nullable
    @Override
    public String getAdobeSiteSubSection() {
        return SITE_SUB_SECTION_EVERYSTUDENT;
    }
}
