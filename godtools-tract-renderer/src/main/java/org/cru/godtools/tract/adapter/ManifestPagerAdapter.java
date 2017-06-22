package org.cru.godtools.tract.adapter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.graphics.Color;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pools;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ccci.gto.android.common.support.v4.adapter.ViewHolderPagerAdapter;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.activity.ModalActivity;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter.PageViewHolder;
import org.cru.godtools.tract.model.CallToAction;
import org.cru.godtools.tract.model.Card;
import org.cru.godtools.tract.model.Card.CardViewHolder;
import org.cru.godtools.tract.model.Header;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.model.Modal;
import org.cru.godtools.tract.model.Page;
import org.cru.godtools.tract.widget.PageContentLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

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

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        EventBus.getDefault().unregister(this);
    }

    /* END lifecycle */

    class PageViewHolder extends ViewHolderPagerAdapter.ViewHolder implements CallToAction.Callbacks,
            CardViewHolder.Callbacks {
        private final Page.PageViewHolder mModelViewHolder;

        @BindView(R2.id.page)
        View mPageView;

        @BindView(R2.id.page_content_layout)
        PageContentLayout mPageContentLayout;

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

        @NonNull
        private final Pools.Pool<CardViewHolder> mRecycledCardViewHolders = new Pools.SimplePool<>(3);
        @NonNull
        private final List<CardViewHolder> mCardViewHolders = new ArrayList<>();

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
            bindCards(page);
            CallToAction.bind(page != null ? page.getCallToAction() : null, mCallToAction, this);
        }

        void onContentEvent(@NonNull final Event event) {
            if (mPage != null) {
                checkForModalEvent(event);
            }
        }

        @Override
        public void onToggleCard(@NonNull final CardViewHolder holder) {
            final Card card = holder.getModel();
            if (card != null) {
                int position = card.getPosition();
                if (position == mPageContentLayout.getActiveCardPosition()) {
                    position = -1;
                }
                mPageContentLayout.changeActiveCard(position, true);
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

        private void bindCards(@Nullable final Page page) {
            final List<Card> cards = page != null ? page.getCards() : Collections.emptyList();
            final ListIterator<CardViewHolder> i = mCardViewHolders.listIterator();

            // update all visible cards
            for (final Card card : cards) {
                if (i.hasNext()) {
                    i.next().bind(card);
                } else {
                    // acquire a view holder
                    CardViewHolder holder = mRecycledCardViewHolders.acquire();
                    if (holder == null) {
                        holder = Card.createViewHolder(mPageContentLayout, mModelViewHolder);
                        holder.setCallbacks(this);
                    }

                    // update holder and add it to the layout
                    holder.bind(card);
                    i.add(holder);
                    mPageContentLayout.addView(holder.mRoot);
                }
            }

            // remove any remaining cards that are no longer used
            while (i.hasNext()) {
                final CardViewHolder holder = i.next();
                mPageContentLayout.removeView(holder.mRoot);
                i.remove();
                holder.bind(null);
                mRecycledCardViewHolders.release(holder);
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
