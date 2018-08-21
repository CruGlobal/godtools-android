package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;

import butterknife.BindView;

final class HeaderViewHolder extends BaseViewHolder<Header> {
    @BindView(R2.id.header_number)
    TextView mHeaderNumber;
    @BindView(R2.id.header_title)
    TextView mHeaderTitle;

    HeaderViewHolder(@NonNull final View root, @Nullable final PageViewHolder parentViewHolder) {
        super(Header.class, root, parentViewHolder);
    }

    @NonNull
    public static HeaderViewHolder forView(@NonNull final View root,
                                           @Nullable final PageViewHolder parentViewHolder) {
        final HeaderViewHolder holder = forView(root, HeaderViewHolder.class);
        return holder != null ? holder : new HeaderViewHolder(root, parentViewHolder);
    }

    /* BEGIN lifecycle */

    @Override
    void onBind() {
        super.onBind();
        bindHeader();
        bindNumber();
        bindTitle();
    }

    /* END lifecycle */

    private void bindHeader() {
        mRoot.setVisibility(mModel != null ? View.VISIBLE : View.GONE);
        mRoot.setBackgroundColor(Header.getBackgroundColor(mModel));
    }

    private void bindNumber() {
        final Text number = mModel != null ? mModel.mNumber : null;
        mHeaderNumber.setVisibility(number != null ? View.VISIBLE : View.GONE);
        TextViewUtils.bind(number, mHeaderNumber, R.dimen.text_size_header_number, null);
    }

    private void bindTitle() {
        final Text title = mModel != null ? mModel.mTitle : null;
        mHeaderTitle.setVisibility(title != null ? View.VISIBLE : View.GONE);
        TextViewUtils.bind(title, mHeaderTitle);
    }
}
