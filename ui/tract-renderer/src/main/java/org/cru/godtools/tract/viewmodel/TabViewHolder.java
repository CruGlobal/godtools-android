package org.cru.godtools.tract.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.model.AnalyticsEvent.Trigger;
import org.cru.godtools.tract.model.Tab;

@UiThread
public final class TabViewHolder extends ParentViewHolder<Tab> {
    TabViewHolder(@NonNull final ViewGroup parent, @Nullable final TabsViewHolder parentViewHolder) {
        super(Tab.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    public void trackSelectedAnalyticsEvents() {
        if (mModel != null) {
            triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.SELECTED, Trigger.DEFAULT);
        }
    }
}
