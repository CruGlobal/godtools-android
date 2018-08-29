package org.cru.godtools.base.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment {
    @Nullable
    private Unbinder mButterKnife;

    // region Lifecycle Events

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButterKnife = ButterKnife.bind(this, view);
    }

    @Override
    public void onDestroyView() {
        if (mButterKnife != null) {
            mButterKnife.unbind();
        }
        mButterKnife = null;
        super.onDestroyView();
    }

    // endregion Lifecycle Events
}
