package org.cru.godtools.tract.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.annimon.stream.Optional;

import org.ccci.gto.android.common.support.v4.util.IdUtils;
import org.ccci.gto.android.common.viewpager.adapter.ViewHolderPagerAdapter;
import org.cru.godtools.api.model.NavigationEvent;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter.RVPageViewHolder;
import org.cru.godtools.tract.viewmodel.PageViewHolder;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Modal;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Collections;
import java.util.List;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.ButterKnife;

public final class ManifestPagerAdapter extends ViewHolderPagerAdapter<RVPageViewHolder> implements
        DefaultLifecycleObserver, Observer<Manifest> {
    public interface Callbacks {
        void goToPage(int position);

        void showModal(@NonNull Modal modal);

        void onUpdateActiveCard(@NonNull Page page, @Nullable Card card);
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
        final List<Page> pages = mManifest != null ? mManifest.getPages() : Collections.emptyList();
        for (int i = 0; i < pages.size(); i++) {
            if (id == IdUtils.convertId(pages.get(i).getId())) {
                return i;
            }
        }
        return POSITION_NONE;
    }

    void dispatchUpdateActiveCard(@NonNull final Page page, @Nullable final Card card) {
        if (mCallbacks != null) {
            mCallbacks.onUpdateActiveCard(page, card);
        }
    }

    // region Lifecycle Events

    @Override
    public void onStart(@NonNull final LifecycleOwner owner) {
        EventBus.getDefault().register(this);
        Optional.ofNullable(getPrimaryItem())
                .ifPresent(RVPageViewHolder::markVisible);
    }

    @Override
    public void onChanged(final Manifest manifest) {
        setManifest(manifest);
    }

    @NonNull
    @Override
    protected RVPageViewHolder onCreateViewHolder(@NonNull final ViewGroup parent) {
        return new RVPageViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.page_manifest_page, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull final RVPageViewHolder holder, final int position) {
        super.onBindViewHolder(holder, position);
        assert mManifest != null;
        holder.onBind(mManifest.getPages().get(position));
    }

    @Override
    protected void onUpdatePrimaryItem(@Nullable final RVPageViewHolder old, @Nullable final RVPageViewHolder current) {
        if (current != null && current.mPage != null) {
            dispatchUpdateActiveCard(current.mPage, current.getActiveCard());
        }

        // update visibility
        if (old != current) {
            if (old != null) {
                old.markHidden();
            }
            if (current != null) {
                current.markVisible();
            }
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        // check for the event on the current page
        final RVPageViewHolder holder = getPrimaryItem();
        if (holder != null) {
            holder.onContentEvent(event);
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onBroadcastEvent(@NonNull final NavigationEvent event) {
        final RVPageViewHolder holder = getPrimaryItem();
        if (holder != null) {
            holder.onBroadcastEvent(event);
        }
    }

    @Override
    protected void onViewHolderRecycled(@NonNull final RVPageViewHolder holder) {
        super.onViewHolderRecycled(holder);
        holder.onBind(null);
    }

    @Override
    public void onStop(@NonNull final LifecycleOwner owner) {
        Optional.ofNullable(getPrimaryItem())
                .ifPresent(RVPageViewHolder::markHidden);
        EventBus.getDefault().unregister(this);
    }

    // endregion Lifecycle Events

    public class RVPageViewHolder extends ViewHolderPagerAdapter.ViewHolder implements PageViewHolder.Callbacks {
        private final PageViewHolder mModelViewHolder;

        @BindView(R2.id.page)
        View mPageView;

        @Nullable
        Page mPage;

        RVPageViewHolder(@NonNull final View view) {
            super(view);
            ButterKnife.bind(this, view);
            mModelViewHolder = PageViewHolder.forView(view);
            mModelViewHolder.setCallbacks(this);
        }

        // region Lifecycle Events

        void onBind(@Nullable final Page page) {
            // short-circuit if we aren't changing the page
            if (page == mPage) {
                return;
            }
            mPage = page;
            mModelViewHolder.bind(page);
        }

        void onContentEvent(@NonNull final Event event) {
            mModelViewHolder.onContentEvent(event);
            if (mPage != null) {
                checkForModalEvent(event);
            }
        }

        void onBroadcastEvent(@NonNull final NavigationEvent event) {
            mModelViewHolder.onBroadcastEvent(event);
        }

        @Override
        public void onUpdateActiveCard(@Nullable final Card card) {
            if (getPrimaryItem() == this) {
                if (mPage != null) {
                    dispatchUpdateActiveCard(mPage, card);
                }
            }
        }

        // endregion Lifecycle Events

        private void checkForModalEvent(@NonNull final Event event) {
            assert mPage != null;
            for (final Modal modal : mPage.getModals()) {
                if (modal.getListeners().contains(event.id)) {
                    if (mCallbacks != null) {
                        mCallbacks.showModal(modal);
                    }
                }
            }
        }

        @Nullable
        public Page getPage() {
            return mPage;
        }

        @Nullable
        public Card getActiveCard() {
            return mModelViewHolder.getActiveCard();
        }

        void markVisible() {
            mModelViewHolder.markVisible();
        }

        void markHidden() {
            mModelViewHolder.markHidden();
        }

        @Override
        public void goToNextPage() {
            if (mCallbacks != null && mPage != null) {
                mCallbacks.goToPage(mPage.getPosition() + 1);
            }
        }
    }
}
