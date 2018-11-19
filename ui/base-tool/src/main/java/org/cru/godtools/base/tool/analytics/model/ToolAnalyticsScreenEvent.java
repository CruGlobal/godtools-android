package org.cru.godtools.base.tool.analytics.model;

import org.cru.godtools.analytics.model.AnalyticsScreenEvent;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ToolAnalyticsScreenEvent extends AnalyticsScreenEvent {
    public static final String SCREEN_CATEGORIES = "Categories";

    @Nullable
    private final String mTool;

    public ToolAnalyticsScreenEvent(@NonNull final String screen, @Nullable final String tool,
                                    @Nullable final Locale locale) {
        super(screen, locale);
        mTool = tool;
    }

    @Nullable
    @Override
    public String getAdobeSiteSection() {
        return mTool;
    }
}
