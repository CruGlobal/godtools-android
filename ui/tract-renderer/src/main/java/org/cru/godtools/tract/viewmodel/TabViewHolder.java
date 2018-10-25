package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;

import org.cru.godtools.tract.R;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

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
