package org.cru.godtools.tract.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView;
import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter.PageViewHolder;
import org.cru.godtools.tract.model.Header;
import org.cru.godtools.tract.model.Hero;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.model.Page;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public final class ManifestPagerAdapter extends ViewHolderPagerAdapter<PageViewHolder> {
    @Nullable
    private Manifest mManifest;

    public void setManifest(@Nullable final Manifest manifest) {
        final Manifest old = mManifest;
        mManifest = manifest;
        if (old != mManifest) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return mManifest != null ? mManifest.getPages().size() : 0;
    }

    @NonNull
    @Override
    protected PageViewHolder onCreateViewHolder(@NonNull final ViewGroup parent) {
        return new PageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.page_manifest_page, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final PageViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        assert mManifest != null;
        holder.bind(mManifest.getPages().get(position));
    }

    class PageViewHolder extends ViewHolderPagerAdapter.ViewHolder {
        @BindView(R2.id.page)
        View mPageView;
        @BindView(R2.id.background_image)
        SimplePicassoImageView mBackgroundImage;

        @BindView(R2.id.header)
        View mHeader;
        @BindView(R2.id.header_number)
        TextView mHeaderNumber;
        @BindView(R2.id.header_title)
        TextView mHeaderTitle;

        @Nullable
        @BindView(R2.id.hero)
        View mHero;

        @BindViews({R2.id.header, R2.id.header_number, R2.id.header_title})
        List<View> mHeaderViews;

        @Nullable
        Page mPage;

        PageViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        void bind(@Nullable final Page page) {
            // short-circuit if we aren't changing the page
            if (page == mPage) {
                return;
            }
            mPage = page;

            bindPage(page);
            bindHeader(page);
            Hero.bind(page != null ? page.getHero() : null, mHero);
        }

        private void bindPage(@Nullable final Page page) {
            mPageView.setBackgroundColor(Page.getBackgroundColor(page));
            Page.bindBackgroundImage(page, mBackgroundImage);
        }

        private void bindHeader(@Nullable final Page page) {
            final Header header = page != null ? page.getHeader() : null;

            ButterKnife.apply(mHeaderViews, (ButterKnife.Action<View>) (view, i) -> view
                    .setVisibility(header != null ? View.VISIBLE : View.GONE));

            if (header != null) {
                mHeader.setBackgroundColor(header.getBackgroundColor());
                header.bindNumber(mHeaderNumber);
                header.bindTitle(mHeaderTitle);
            } else {
                mHeader.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
