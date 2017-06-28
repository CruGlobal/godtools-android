package org.cru.godtools.tract.adapter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Color;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;

import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.ccci.gto.android.common.support.v4.util.IdUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.activity.ModalActivity;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter.PageViewHolder;
import org.cru.godtools.tract.model.CallToAction;
import org.cru.godtools.tract.model.Header;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.model.Modal;
import org.cru.godtools.tract.model.Page;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public final class ManifestPagerAdapter extends ViewHolderPagerAdapter<PageViewHolder> implements LifecycleObserver {
    public interface Callbacks {
        void goToPage(int position);
    }

    @Nullable
    Callbacks mCallbacks;

    @Nullable
    private Manifest mManifest;

    public ManifestPagerAdapter() {
        setHasStableIds(true);
    }

    public void setCallbacks(@Nullable final Callbacks callbacks) {
        mCallbacks = callbacks;
    }

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

    @Override
    public long getItemId(final int position) {
        assert mManifest != null : "there are no items when the manifest is null";
        return IdUtils.convertId(mManifest.getPages().get(position).getId());
    }

    @Override
    protected int getItemPositionFromId(final long id) {
        final List<Page> pages = mManifest != null ? mManifest.getPages() : ImmutableList.of();
        for (int i = 0; i < pages.size(); i++) {
            if (id == IdUtils.convertId(pages.get(i).getId())) {
                return i;
            }
        }
        return POSITION_NONE;
    }

    /* BEGIN lifecycle */

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        EventBus.getDefault().register(this);
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
        holder.onBind(mManifest.getPages().get(position));
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        // check for the event on the current page
        final PageViewHolder holder = getPrimaryItem();
        if (holder != null) {
            holder.onContentEvent(event);
        }
    }

    @Override
    protected void onViewHolderRecycled(@NonNull final PageViewHolder holder) {
        super.onViewHolderRecycled(holder);
        holder.onBind(null);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        EventBus.getDefault().unregister(this);
    }

    /* END lifecycle */

    class PageViewHolder extends ViewHolderPagerAdapter.ViewHolder implements CallToAction.Callbacks {
        private final Page.PageViewHolder mModelViewHolder;

        @BindView(R2.id.page)
        View mPageView;

        // Header & Hero
        @BindView(R2.id.initial_page_content)
        NestedScrollView mHeaderAndHeroLayout;
        @BindView(R2.id.header)
        View mHeader;
        @BindView(R2.id.header_number)
        TextView mHeaderNumber;
        @BindView(R2.id.header_title)
        TextView mHeaderTitle;

        // call to action
        @BindView(R2.id.call_to_action)
        View mCallToAction;

        @BindViews({R2.id.header, R2.id.header_number, R2.id.header_title})
        List<View> mHeaderViews;

        @Nullable
        Page mPage;

        PageViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mModelViewHolder = Page.getViewHolder(view);
        }

        /* BEGIN lifecycle */

        void onBind(@Nullable final Page page) {
            // short-circuit if we aren't changing the page
            if (page == mPage) {
                return;
            }
            mPage = page;
            mModelViewHolder.bind(page);

            bindHeader(page);
            CallToAction.bind(page != null ? page.getCallToAction() : null, mCallToAction, this);
        }

        void onContentEvent(@NonNull final Event event) {
            mModelViewHolder.onContentEvent(event);
            if (mPage != null) {
                checkForModalEvent(event);
            }
        }

        /* END lifecycle */

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

        private void checkForModalEvent(@NonNull final Event event) {
            assert mPage != null;
            final Manifest manifest = mPage.getManifest();
            for (final Modal modal : mPage.getModals()) {
                if (modal.getListeners().contains(event.id)) {
                    ModalActivity.start(mPageView.getContext(), manifest.getManifestName(), manifest.getToolId(),
                                        manifest.getLocale(), mPage.getId(), modal.getId());
                }
            }
        }

        @Override
        public void goToNextPage() {
            if (mCallbacks != null && mPage != null) {
                mCallbacks.goToPage(mPage.getPosition() + 1);
            }
        }
    }
}
