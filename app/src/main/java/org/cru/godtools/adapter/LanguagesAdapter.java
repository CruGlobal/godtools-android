package org.cru.godtools.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.google.common.base.Objects;

import org.cru.godtools.R;
import org.cru.godtools.adapter.LanguagesAdapter.LanguageViewHolder;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Language;
import org.keynote.godtools.android.db.GodToolsDao;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import butterknife.ViewCollections;

import static org.cru.godtools.butterknife.Setters.TINT_LIST;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguageViewHolder> {
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
    @Nullable
    Locale mSelected;
    @NonNull
    Set<Locale> mDisabled = Collections.emptySet();
    @NonNull
    Set<Locale> mProtected = Collections.emptySet();

    public LanguagesAdapter(@NonNull final Context context) {
        setHasStableIds(true);
        mDao = GodToolsDao.getInstance(context);
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
        mSelected = selected;
        notifyItemRangeChanged(0, getItemCount());
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
    public LanguageViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_language, parent, false);
        return new LanguageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LanguageViewHolder holder, int position) {
        holder.bind(position);
    }

    class LanguageViewHolder extends BaseViewHolder {
        @BindColor(R.color.states_remove)
        ColorStateList mActionRemoveTint;

        @Nullable
        @BindView(R.id.root)
        View mRoot;
        @Nullable
        @BindView(R.id.title)
        TextView mTitle;
        @Nullable
        @BindView(R.id.action_add)
        View mActionAdd;
        @Nullable
        @BindView(R.id.action_remove)
        ImageView mActionRemove;

        @Nullable
        Locale mLocale = null;
        boolean mAdded = false;

        LanguageViewHolder(@NonNull final View view) {
            super(view);
            if (mActionRemove != null) {
                ViewCollections.set(mActionRemove, TINT_LIST, mActionRemoveTint);
            }
        }

        @Override
        void bind(final int position) {
            final Language language = mShowNone && position == 0 ? null :
                    mLanguages.get(position - (mShowNone ? 1 : 0));
            mLocale = language != null ? language.getCode() : null;
            mAdded = language != null && language.isAdded();

            if (mRoot != null) {
                mRoot.setSelected(Objects.equal(mSelected, mLocale));
            }
            if (mTitle != null) {
                if (mLocale != null) {
                    mTitle.setText(mLocale.getDisplayName());
                } else {
                    mTitle.setText(R.string.label_language_none);
                }
            }
            if (mActionAdd != null) {
                mActionAdd.setVisibility(mAdded || mLocale == null ? View.GONE : View.VISIBLE);
            }
            if (mActionRemove != null) {
                mActionRemove.setEnabled(!mProtected.contains(mLocale));
                mActionRemove.setVisibility(mAdded ? View.VISIBLE : View.GONE);
            }
        }

        @Optional
        @OnClick(R.id.root)
        void onSelectLanguage() {
            if (mCallbacks != null) {
                if (!mDisabled.contains(mLocale)) {
                    mCallbacks.onLanguageSelected(mLocale);
                } else {
                    // TODO: toast: You cannot select this language.
                }
            }
        }

        @Optional
        @OnClick(R.id.action_remove)
        void onRemoveLanguage() {
            if (!mProtected.contains(mLocale)) {
                mTools.removeLanguage(mLocale);
            } else {
                // TODO: toast: You cannot remove this language from the device
            }
        }
    }
}
