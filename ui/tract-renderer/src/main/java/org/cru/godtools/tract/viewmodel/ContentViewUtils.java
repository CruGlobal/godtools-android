package org.cru.godtools.tract.viewmodel;

import android.view.ViewGroup;

import org.ccci.gto.android.common.app.ApplicationUtils;
import org.cru.godtools.tract.ui.controller.ParagraphController;
import org.cru.godtools.xml.model.Button;
import org.cru.godtools.xml.model.Content;
import org.cru.godtools.xml.model.Form;
import org.cru.godtools.xml.model.Image;
import org.cru.godtools.xml.model.Input;
import org.cru.godtools.xml.model.Link;
import org.cru.godtools.xml.model.Paragraph;
import org.cru.godtools.xml.model.Tabs;
import org.cru.godtools.xml.model.Text;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import timber.log.Timber;

public class ContentViewUtils {
    @Nullable
    public static BaseViewHolder createViewHolder(@NonNull final Class<? extends Content> clazz,
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
            return new ParagraphController(parent, parentViewHolder);
        } else if (Tabs.class.equals(clazz)) {
            return new TabsViewHolder(parent, parentViewHolder);
        } else if (Text.class.equals(clazz)) {
            return new TextViewHolder(parent, parentViewHolder);
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
