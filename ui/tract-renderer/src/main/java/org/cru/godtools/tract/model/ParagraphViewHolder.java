package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.ViewGroup;

import org.cru.godtools.tract.R;

@UiThread
public final class ParagraphViewHolder extends ParentViewHolder<Paragraph> {
    ParagraphViewHolder(@NonNull final ViewGroup parent, @Nullable final BaseViewHolder parentViewHolder) {
        super(Paragraph.class, parent, R.layout.tract_content_paragraph, parentViewHolder);
    }
}
