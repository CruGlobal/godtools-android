package org.cru.godtools.tract.viewmodel;

import android.view.View;

import org.cru.godtools.xml.model.Modal;

import androidx.annotation.NonNull;

public class ModalViewHolder extends ParentViewHolder<Modal> {
    ModalViewHolder(@NonNull final View root) {
        super(Modal.class, root, null);
    }

    @NonNull
    public static ModalViewHolder forView(@NonNull final View root) {
        final ModalViewHolder holder = forView(root, ModalViewHolder.class);
        return holder != null ? holder : new ModalViewHolder(root);
    }
}
