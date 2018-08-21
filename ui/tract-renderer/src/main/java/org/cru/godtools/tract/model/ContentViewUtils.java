package org.cru.godtools.tract.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import org.ccci.gto.android.common.app.ApplicationUtils;

import timber.log.Timber;

public class ContentViewUtils {
    @Nullable
    static BaseViewHolder createViewHolder(@NonNull final Class<? extends Content> clazz,
                                           @NonNull final ViewGroup parent,
                                           @Nullable final BaseViewHolder parentViewHolder) {
        if (Button.class.equals(clazz)) {
            return new ButtonViewHolder(parent, parentViewHolder);
        } else if (Form.class.equals(clazz)) {
            return new FormViewHolder(parent, parentViewHolder);
        } else if (Image.class.equals(clazz)) {
            return new ImageViewHolder(parent, parentViewHolder);
        } else if (Input.class.equals(clazz)) {
            return new InputViewHolder(parent, parentViewHolder);
        } else if (Link.class.equals(clazz)) {
            return new LinkViewHolder(parent, parentViewHolder);
        } else if (Paragraph.class.equals(clazz)) {
            return new Paragraph.ParagraphViewHolder(parent, parentViewHolder);
        } else if (Tabs.class.equals(clazz)) {
            return new Tabs.TabsViewHolder(parent, parentViewHolder);
        } else if (Text.class.equals(clazz)) {
            return new Text.TextViewHolder(parent, parentViewHolder);
        } else {
            final IllegalArgumentException e =
                    new IllegalArgumentException("Unsupported Content class specified: " + clazz.getName());
            if (ApplicationUtils.isDebuggable(parent.getContext())) {
                throw e;
            } else {
                Timber.e(e, "Unsupported Content class specified: %s", clazz.getName());
                return null;
            }
        }
    }
}
