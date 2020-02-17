package org.cru.godtools.tract.viewmodel;

import android.view.View;
import android.widget.TextView;

import org.cru.godtools.base.tool.model.view.TextViewUtils;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.Header;
import org.cru.godtools.xml.model.HeaderKt;
import org.cru.godtools.xml.model.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
        mRoot.setBackgroundColor(HeaderKt.getBackgroundColor(mModel));
    }

    private void bindNumber() {
        final Text number = mModel != null ? mModel.getNumber() : null;
        mHeaderNumber.setVisibility(number != null ? View.VISIBLE : View.GONE);
        TextViewUtils.bind(number, mHeaderNumber, R.dimen.text_size_header_number, null);
    }

    private void bindTitle() {
        final Text title = mModel != null ? mModel.getTitle() : null;
        mHeaderTitle.setVisibility(title != null ? View.VISIBLE : View.GONE);
        TextViewUtils.bind(title, mHeaderTitle);
    }
}
