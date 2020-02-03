package org.cru.godtools.tract.viewmodel;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.base.tool.analytics.model.ExitLinkActionEvent;
import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.base.ui.util.WebUrlLauncher;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.AnalyticsEvent.Trigger;
import org.cru.godtools.xml.model.Button;
import org.cru.godtools.xml.model.Text;
import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.view.ViewCompat;
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
        final Text text = mModel != null ? mModel.getText() : null;
        TextViewUtils.bind(text, mButton);
        ViewCompat.setBackgroundTintList(mButton, ColorStateList.valueOf(Button.getButtonColor(mModel)));
    }

    // endregion Lifecycle Events

    @OnClick(R2.id.button)
    void click() {
        if (mModel != null) {
            triggerAnalyticsEvents(mModel.getAnalyticsEvents(), Trigger.SELECTED, Trigger.DEFAULT);

            switch (mModel.getType()) {
                case URL:
                    final Uri url = mModel.getUrl();
                    if (url != null) {
                        final Context context = mRoot.getContext();
                        EventBus.getDefault().post(new ExitLinkActionEvent(url));
                        WebUrlLauncher.openUrl(context, url);
                    }
                    break;
                case EVENT:
                    sendEvents(mModel.getEvents());
                    break;
            }
        }
    }
}
