package org.cru.godtools.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder;
import org.cru.godtools.databinding.ListItemLanguageBinding;
import org.cru.godtools.model.Language;
import org.cru.godtools.ui.languages.LanguageSelectedListener;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

public class LanguagesAdapter extends RecyclerView.Adapter<DataBindingViewHolder<ListItemLanguageBinding>> implements LanguageSelectedListener {
    public interface Callbacks {
        void onLanguageSelected(@Nullable Locale language);
    }

    @Nullable
    Callbacks mCallbacks;

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

    public void setCallbacks(@Nullable final Callbacks callbacks) {
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

    @Override
    public DataBindingViewHolder<ListItemLanguageBinding> onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        final ListItemLanguageBinding binding =
                ListItemLanguageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        binding.setListener(this);
        binding.setSelected(mSelected);
        return new DataBindingViewHolder<>(binding);
    }

    @Override
    public void onBindViewHolder(DataBindingViewHolder<ListItemLanguageBinding> holder, int position) {
        holder.getBinding()
                .setLanguage(mShowNone && position == 0 ? null : mLanguages.get(position - (mShowNone ? 1 : 0)));
        holder.getBinding().executePendingBindings();
    }

    @Override
    public void onLanguageSelected(@Nullable final Language language) {
        if (mCallbacks != null) {
            final Locale locale = language != null ? language.getCode() : null;
            if (!mDisabled.contains(locale)) {
                mCallbacks.onLanguageSelected(locale);
            } else {
                // TODO: toast: You cannot select this language.
            }
        }
    }
}
