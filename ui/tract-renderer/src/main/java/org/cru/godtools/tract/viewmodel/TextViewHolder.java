package org.cru.godtools.tract.viewmodel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.xml.model.Text;

import butterknife.BindView;

@UiThread
final class TextViewHolder extends BaseViewHolder<Text> {
    @BindView(R2.id.content_text)
    TextView mText;

    TextViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Text.class, parent, R.layout.tract_content_text, parentViewHolder);
    }

    @Override
    void onBind() {
        super.onBind();
        TextViewUtils.bind(mModel, mText);
    }
}
