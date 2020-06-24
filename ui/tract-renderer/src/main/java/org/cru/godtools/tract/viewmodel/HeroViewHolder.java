package org.cru.godtools.tract.viewmodel;

import android.view.View;

import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Hero;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class HeroViewHolder extends ParentViewHolder<Hero> {
    @Nullable
    private List<Runnable> mPendingAnalyticsEvents;

    HeroViewHolder(@NonNull final View root, @Nullable final PageViewHolder parentViewHolder) {
        super(Hero.class, root, parentViewHolder);
    }

    @NonNull
    public static HeroViewHolder forView(@NonNull final View root,
                                         @Nullable final PageViewHolder parentViewHolder) {
        final HeroViewHolder holder = forView(root, HeroViewHolder.class);
        return holder != null ? holder : new HeroViewHolder(root, parentViewHolder);
    }

    // region Lifecycle
    @Override
    void onVisible() {
        super.onVisible();
        if (mModel != null) {
            mPendingAnalyticsEvents =
                    triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.VISIBLE, Trigger.DEFAULT);
        }
    }

    @Override
    void onHidden() {
        super.onHidden();
        if (mPendingAnalyticsEvents != null) {
            cancelPendingAnalyticsEvents(mPendingAnalyticsEvents);
        }
    }
    // endregion Lifecycle
}
