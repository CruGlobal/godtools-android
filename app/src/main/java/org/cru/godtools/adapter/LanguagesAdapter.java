package org.cru.godtools.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter;
import org.cru.godtools.databinding.ListItemLanguageBinding;
import org.cru.godtools.model.Language;
import org.cru.godtools.ui.languages.LanguageSelectedListener;
import org.cru.godtools.ui.languages.LocaleSelectedListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;

public class LanguagesAdapter extends SimpleDataBindingAdapter<ListItemLanguageBinding>
        implements LanguageSelectedListener {
    @Nullable
    LocaleSelectedListener mCallbacks;

    final boolean mShowNone;

    @NonNull
    List<Language> mLanguages = Collections.emptyList();
    private final ObservableField<Locale> mSelected = new ObservableField<>();
    @NonNull
    Set<Locale> mDisabled = Collections.emptySet();

    public LanguagesAdapter(final boolean showNone) {
        setHasStableIds(true);
        mShowNone = showNone;
    }

    public void setCallbacks(@Nullable final LocaleSelectedListener callbacks) {
        mCallbacks = callbacks;
    }

    public void setLanguages(@Nullable final List<Language> languages) {
        mLanguages = languages != null ? languages : Collections.emptyList();
        notifyDataSetChanged();
    }

    public void setSelected(@Nullable final Locale selected) {
        mSelected.set(selected);
    }

    public void setDisabled(@NonNull final Locale... disabled) {
        mDisabled = Stream.of(disabled)
                .withoutNulls()
                .collect(Collectors.toSet());
        notifyItemRangeChanged(0, getItemCount());
    }

    @Override
    public int getItemCount() {
        return mLanguages.size() + (mShowNone ? 1 : 0);
    }

    @Override
    public long getItemId(final int position) {
        if (mShowNone && position == 0) {
            return View.NO_ID;
        }
        return mLanguages.get(position - (mShowNone ? 1 : 0)).getId();
    }

    @NonNull
    @Override
    protected ListItemLanguageBinding onCreateViewDataBinding(@NotNull final ViewGroup parent, final int viewType) {
        final ListItemLanguageBinding binding =
                ListItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        binding.setListener(this);
        binding.setSelected(mSelected);
        return binding;
    }

    @Override
    protected void onBindViewDataBinding(@NotNull final ListItemLanguageBinding binding, final int position) {
        binding.setLanguage(mShowNone && position == 0 ? null : mLanguages.get(position - (mShowNone ? 1 : 0)));
    }

    @Override
    public void onLanguageSelected(@Nullable final Language language) {
        if (mCallbacks != null) {
            final Locale locale = language != null ? language.getCode() : null;
            if (!mDisabled.contains(locale)) {
                mCallbacks.onLocaleSelected(locale);
            } else {
                // TODO: toast: You cannot select this language.
            }
        }
    }
}
