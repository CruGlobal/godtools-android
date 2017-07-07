package org.cru.godtools.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.keynote.godtools.android.R;

public class AboutFragment extends Fragment {
    @NonNull
    public static Fragment newInstance() {
        return new AboutFragment();
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @NonNull final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    /* END lifecycle */
}
