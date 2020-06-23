package org.cru.godtools.tract.viewmodel;

import android.view.View;
import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Hero;
import org.cru.godtools.xml.model.StylesKt;
import org.cru.godtools.xml.model.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;

public class HeroViewHolder extends ParentViewHolder<Hero> {
    @BindView(R2.id.hero_heading)
    TextView mHeading;

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

    // endregion Lifecycle Events

    private void bindHeading() {
        final Text heading = mModel != null ? mModel.getHeading() : null;
        TextViewUtils.bind(heading, mHeading, R.dimen.text_size_hero_heading, StylesKt.getPrimaryColor(mModel));
        mHeading.setVisibility(heading != null ? View.VISIBLE : View.GONE);
    }
}
