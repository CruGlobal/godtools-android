package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;

import butterknife.BindView;
import butterknife.OnClick;

@UiThread
final class LinkViewHolder extends BaseViewHolder<Link> {
    @BindView(R2.id.content_link)
    TextView mLink;

    LinkViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Link.class, parent, R.layout.tract_content_link, parentViewHolder);
    }

    // region Lifecycle Events

    @Override
    void onBind() {
        super.onBind();
        bindText();
    }

    // endregion Lifecycle Events

    private void bindText() {
        final Text text = mModel != null ? mModel.mText : null;
        TextViewUtils.bind(text, mLink, null, Styles.getPrimaryColor(Base.getStylesParent(mModel)));
    }

    @OnClick(R2.id.content_link)
    void click() {
        if (mModel != null) {
            sendEvents(mModel.mEvents);
            triggerAnalyticsEvents(mModel.mAnalyticsEvents, AnalyticsEvent.Trigger.SELECTED, AnalyticsEvent.Trigger.DEFAULT);
        }
    }
}
