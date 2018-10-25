package org.cru.godtools.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.ButterKnife;

abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    BaseViewHolder(@NonNull final View rootView) {
        super(rootView);
        ButterKnife.bind(this, rootView);
    }

    void bind(final int position) {}
}
