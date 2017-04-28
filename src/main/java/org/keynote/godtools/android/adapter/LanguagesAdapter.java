package org.keynote.godtools.android.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Objects;

import org.keynote.godtools.android.R;
import org.keynote.godtools.android.adapter.LanguagesAdapter.LanguageViewHolder;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Language;
import org.keynote.godtools.android.service.GodToolsResourceManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;

public class LanguagesAdapter extends RecyclerView.Adapter<LanguageViewHolder> {
    public interface Callbacks {
        void onLanguageSelected(@NonNull Locale language);
    }

    @NonNull
    final GodToolsDao mDao;
    @NonNull
    final GodToolsResourceManager mResources;

    @Nullable
    Callbacks mCallbacks;

    @NonNull
    List<Language> mLanguages = Collections.emptyList();
    @Nullable
    Locale mSelected;

    public LanguagesAdapter(@NonNull final Context context) {
        mDao = GodToolsDao.getInstance(context);
        mResources = GodToolsResourceManager.getInstance(context);
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    public void setLanguages(@Nullable final List<Language> languages) {
        mLanguages = languages != null ? languages : Collections.emptyList();
        notifyDataSetChanged();
    }

    public void setSelected(@Nullable final Locale selected) {
        mSelected = selected;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mLanguages.size();
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
        View mActionRemove;

        @Nullable
        Locale mLocale = null;
        boolean mAdded = false;

        LanguageViewHolder(@NonNull final View view) {
            super(view);
        }

        @Override
        void bind(final int position) {
            final Language language = mLanguages.get(position);
            mLocale = language != null ? language.getCode() : null;
            mAdded = language != null && language.isAdded();

            if (mRoot != null) {
                mRoot.setSelected(Objects.equal(mSelected, mLocale));
            }
            if (mTitle != null) {
                mTitle.setText(mLocale != null ? mLocale.getDisplayName() : "");
            }
            if (mActionAdd != null) {
                mActionAdd.setVisibility(mAdded ? View.GONE : View.VISIBLE);
            }
            if (mActionRemove != null) {
                mActionRemove.setVisibility(mAdded ? View.VISIBLE : View.GONE);
            }
        }

        @Optional
        @OnClick(R.id.root)
        void onSelectLanguage() {
            if (mCallbacks != null) {
                if (mLocale != null) {
                    mCallbacks.onLanguageSelected(mLocale);
                }
            }
        }

        @Optional
        @UiThread
        @OnClick(R.id.action_add)
        void onAddLanguage() {
            mResources.addLanguage(mLocale);
        }

        @Optional
        @OnClick(R.id.action_remove)
        void onRemoveLanguage() {
            mResources.removeLanguage(mLocale);
        }
    }
}
