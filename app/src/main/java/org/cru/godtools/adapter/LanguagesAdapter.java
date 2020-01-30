package org.cru.godtools.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.ccci.gto.android.common.recyclerview.adapter.DataBindingViewHolder;
import org.cru.godtools.databinding.ListItemLanguageBinding;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Language;
import org.cru.godtools.ui.languages.LanguageSelectedListener;
import org.keynote.godtools.android.db.GodToolsDao;

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

    @NonNull
    final GodToolsDao mDao;
    @NonNull
    final GodToolsDownloadManager mTools;

    @Nullable
    Callbacks mCallbacks;

    boolean mShowNone = false;

    @NonNull
    List<Language> mLanguages = Collections.emptyList();
    private final ObservableField<Locale> mSelected = new ObservableField<>();
    @NonNull
    Set<Locale> mDisabled = Collections.emptySet();
    @NonNull
    Set<Locale> mProtected = Collections.emptySet();

    public LanguagesAdapter(@NonNull final Context context) {
        setHasStableIds(true);
        mDao = GodToolsDao.Companion.getInstance(context);
        mTools = GodToolsDownloadManager.getInstance(context);
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void setShowNone(final boolean state) {
        final boolean old = mShowNone;
        mShowNone = state;
        if (old != mShowNone) {
            if (mShowNone) {
                notifyItemInserted(0);
            } else {
                notifyItemRemoved(0);
            }
        }
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

    public void setProtected(@Nullable final Set<Locale> languages) {
        mProtected = languages != null ? languages : Collections.emptySet();
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
