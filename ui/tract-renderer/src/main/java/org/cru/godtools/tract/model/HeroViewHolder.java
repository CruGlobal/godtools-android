package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;

import java.util.List;

import butterknife.BindView;

public class HeroViewHolder extends ParentViewHolder<Hero> {
    @BindView(R2.id.hero_heading)
    TextView mHeading;

    @Nullable
    private List<Runnable> mPendingAnalyticsEvents;

    HeroViewHolder(@NonNull final View root, @Nullable final BaseViewHolder parentViewHolder) {
        super(Hero.class, root, parentViewHolder);
    }

    @NonNull
    public static HeroViewHolder forView(@NonNull final View root,
                                         @Nullable final PageViewHolder parentViewHolder) {
        final HeroViewHolder holder = forView(root, HeroViewHolder.class);
        return holder != null ? holder : new HeroViewHolder(root, parentViewHolder);
    }

    // region Lifecycle Events

    @Override
    void onBind() {
        super.onBind();
        mRoot.setVisibility(mModel != null ? View.VISIBLE : View.GONE);
        bindHeading();
    }

    @Override
    void onVisible() {
        super.onVisible();
        if (mModel != null) {
            mPendingAnalyticsEvents =
                    triggerAnalyticsEvents(mModel.mAnalyticsEvents, AnalyticsEvent.Trigger.VISIBLE, AnalyticsEvent.Trigger.DEFAULT);
        }
    }

    @Override
    void onHidden() {
        super.onHidden();
        if (mPendingAnalyticsEvents != null) {
            cancelPendingAnalyticsEvents(mPendingAnalyticsEvents);
        }
    }

    // endregion Lifecycle Events

    private void bindHeading() {
        final Text heading = mModel != null ? mModel.mHeading : null;
        TextViewUtils.bind(heading, mHeading, R.dimen.text_size_hero_heading, Styles.getPrimaryColor(mModel));
        mHeading.setVisibility(heading != null ? View.VISIBLE : View.GONE);
    }
}
