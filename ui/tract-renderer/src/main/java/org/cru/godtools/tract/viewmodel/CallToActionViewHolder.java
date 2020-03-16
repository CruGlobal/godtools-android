package org.cru.godtools.tract.viewmodel;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.base.ui.util.DrawableUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.CallToAction;
import org.cru.godtools.xml.model.CallToActionKt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import butterknife.BindView;
import butterknife.OnClick;

final class CallToActionViewHolder extends BaseViewHolder<CallToAction> {
    public interface Callbacks {
        void goToNextPage();
    }

    @BindView(R2.id.call_to_action_label)
    TextView mLabelView;
    @BindView(R2.id.call_to_action_arrow)
    ImageView mArrowView;

    @Nullable
    private Callbacks mCallbacks;

    CallToActionViewHolder(@NonNull final View root, @Nullable final BaseViewHolder parentViewHolder) {
        super(CallToAction.class, root, parentViewHolder);
    }

    @NonNull
    public static CallToActionViewHolder forView(@NonNull final View root,
                                                 @Nullable final PageViewHolder parentViewHolder) {
        final CallToActionViewHolder holder = forView(root, CallToActionViewHolder.class);
        return holder != null ? holder : new CallToActionViewHolder(root, parentViewHolder);
    }

    /* BEGIN lifecycle */

    @Override
    void onBind() {
        super.onBind();
        bindLabel();
        bindArrow();
    }

    @OnClick(R2.id.call_to_action_arrow)
    void onTrigger() {
        if (mCallbacks != null && (mModel == null || mModel.getEvents().isEmpty())) {
            mCallbacks.goToNextPage();
        } else if (mModel != null && !mModel.getEvents().isEmpty()) {
            //TODO: trigger events
        }
    }

    /* END lifecycle */

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    private void bindLabel() {
        TextViewUtils.bind(mModel != null ? mModel.getLabel() : null, mLabelView);
    }

    private void bindArrow() {
        final boolean visible = mModel == null || !mModel.getPage().isLastPage() || !mModel.getEvents().isEmpty();
        mArrowView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mArrowView.setImageResource(R.drawable.ic_call_to_action);
        mArrowView
                .setImageDrawable(DrawableUtils.tint(mArrowView.getDrawable(), CallToActionKt.getControlColor(mModel)));
    }

    @Override
    protected void updateLayoutDirection() {
        // force CallToAction to inherit it's layout direction
        ViewCompat.setLayoutDirection(mRoot, ViewCompat.LAYOUT_DIRECTION_INHERIT);
    }
}
