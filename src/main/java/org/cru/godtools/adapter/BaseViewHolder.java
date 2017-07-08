package org.cru.godtools.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.ButterKnife;

abstract class BaseViewHolder extends RecyclerView.ViewHolder {
    BaseViewHolder(@NonNull final View rootView) {
        super(rootView);
        ButterKnife.bind(this, rootView);
    }

    void bind(final int position) {}
}
