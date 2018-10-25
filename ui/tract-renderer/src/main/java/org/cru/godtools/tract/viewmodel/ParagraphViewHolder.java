package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;

import org.cru.godtools.tract.R;
import org.cru.godtools.xml.model.Paragraph;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

@UiThread
public final class ParagraphViewHolder extends ParentViewHolder<Paragraph> {
    ParagraphViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Paragraph.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }
}
