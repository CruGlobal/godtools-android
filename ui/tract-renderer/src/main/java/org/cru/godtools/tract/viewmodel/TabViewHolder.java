package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.ui.controller.ParentController;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;

@UiThread
public final class TabViewHolder extends ParentController<Tab> {
    TabViewHolder(@NonNull final ViewGroup parent, @Nullable final TabsViewHolder parentViewHolder) {
        super(Tab.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    @BindView(R2.id.content)
    LinearLayout mContent;

    @NonNull
    @Override
    protected LinearLayout getContentContainer() {
        return mContent;
    }

    public void trackSelectedAnalyticsEvents() {
        if (mModel != null) {
            triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.SELECTED, Trigger.DEFAULT);
        }
    }
}
