package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import org.cru.godtools.tract.R;

@UiThread
public final class TabViewHolder extends ParentViewHolder<Tab> {
    TabViewHolder(@NonNull final ViewGroup parent, @Nullable final Tabs.TabsViewHolder parentViewHolder) {
        super(Tab.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    public void trackSelectedAnalyticsEvents() {
        if (mModel != null) {
            triggerAnalyticsEvents(mModel.mAnalyticsEvents, AnalyticsEvent.Trigger.SELECTED, AnalyticsEvent.Trigger.DEFAULT);
        }
    }
}
