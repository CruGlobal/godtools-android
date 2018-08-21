package org.cru.godtools.tract.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.view.ViewCompat;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.analytics.AnalyticsService;
import org.cru.godtools.base.ui.util.WebUrlLauncher;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.model.AnalyticsEvent.Trigger;

import butterknife.BindView;
import butterknife.OnClick;

@UiThread
final class ButtonViewHolder extends BaseViewHolder<Button> {
    @BindView(R2.id.button)
    TextView mButton;

    ButtonViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Button.class, parent, R.layout.tract_content_button, parentViewHolder);
    }

    // region Lifecycle Events

    @Override
    void onBind() {
        super.onBind();
        final Text text = mModel != null ? mModel.mText : null;
        TextViewUtils.bind(text, mButton);
        ViewCompat.setBackgroundTintList(mButton, ColorStateList.valueOf(Button.getButtonColor(mModel)));
    }

    // endregion Lifecycle Events

    @OnClick(R2.id.button)
    void click() {
        if (mModel != null) {
            triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.SELECTED, Trigger.DEFAULT);

            switch (mModel.mType) {
                case URL:
                    if (mModel.mUrl != null) {
                        final Context context = mRoot.getContext();
                        AnalyticsService.getInstance(context).onTrackExitUrl(mModel.mUrl);
                        WebUrlLauncher.openUrl(context, mModel.mUrl);
                    }
                    break;
                case EVENT:
                    sendEvents(mModel.mEvents);
                    break;
            }
        }
    }
}
