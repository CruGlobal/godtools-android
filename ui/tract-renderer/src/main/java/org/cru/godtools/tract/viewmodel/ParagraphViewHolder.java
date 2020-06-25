package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.ui.controller.ParentController;
import org.cru.godtools.xml.model.Paragraph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import butterknife.BindView;

@UiThread
public final class ParagraphViewHolder extends ParentController<Paragraph> {
    ParagraphViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Paragraph.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }

    @BindView(R2.id.content)
    LinearLayout mContent;

    @NonNull
    @Override
    protected LinearLayout getContentContainer() {
        return mContent;
    }
}
