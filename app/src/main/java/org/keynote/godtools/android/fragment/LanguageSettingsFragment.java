package org.keynote.godtools.android.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.cru.godtools.R;
import org.cru.godtools.activity.LanguageSelectionActivity;
import org.keynote.godtools.android.utils.WordUtils;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class LanguageSettingsFragment extends BasePlatformFragment {
    @Nullable
    @BindView(R.id.primary_language)
    TextView mPrimaryLanguageView;
    @Nullable
    @BindView(R.id.parallel_language)
    TextView mParallelLanguageView;

    public static LanguageSettingsFragment newInstance() {
        return new LanguageSettingsFragment();
    }

    /* BEGIN lifecycle */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_language_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateLanguages();
    }

    @Override
    protected void onUpdatePrimaryLanguage() {
        super.onUpdatePrimaryLanguage();
        updateLanguages();
    }

    @Override
    protected void onUpdateParallelLanguage() {
        super.onUpdateParallelLanguage();
        updateLanguages();
    }

    /* END lifecycle */

    private void updateLanguages() {
        if (mPrimaryLanguageView != null) {
            mPrimaryLanguageView.setText(WordUtils.capitalize(mPrimaryLanguage.getDisplayName()));
        }
        if (mParallelLanguageView != null) {
            if (mParallelLanguage != null) {
                mParallelLanguageView.setText(WordUtils.capitalize(mParallelLanguage.getDisplayName()));
            } else {
                mParallelLanguageView.setText(R.string.action_language_parallel_select);
            }
        }
    }

    @Optional
    @OnClick(R.id.primary_language)
    void editPrimaryLanguage() {
        LanguageSelectionActivity.start(getContext(), true);
    }

    @Optional
    @OnClick(R.id.parallel_language)
    void editParallelLanguage() {
        LanguageSelectionActivity.start(getContext(), false);
    }
}
