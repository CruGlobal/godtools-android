package org.cru.godtools.tract.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.annimon.stream.Optional;

import org.ccci.gto.android.common.support.v4.util.IdUtils;
import org.ccci.gto.android.common.viewpager.adapter.BaseDataBindingPagerAdapter;
import org.ccci.gto.android.common.viewpager.adapter.DataBindingViewHolder;
import org.cru.godtools.api.model.NavigationEvent;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter.RVPageViewHolder;
import org.cru.godtools.tract.databinding.TractPageBinding;
import org.cru.godtools.tract.ui.controller.PageController;
import org.cru.godtools.tract.ui.controller.PageControllerKt;
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

public final class ManifestPagerAdapter extends BaseDataBindingPagerAdapter<TractPageBinding, RVPageViewHolder>
        implements PageController.Callbacks, DefaultLifecycleObserver, Observer<Manifest> {
    public interface Callbacks {
        void goToPage(int position);

        void showModal(@NonNull Modal modal);

        void onUpdateActiveCard(@NonNull Page page, @Nullable Card card);
    }

    @NonNull
    private final PageController.Factory mPageControllerFactory;

    @Nullable
    Callbacks mCallbacks;

    @Nullable
    private Manifest mManifest;

    public ManifestPagerAdapter(@NonNull final LifecycleOwner lifecycleOwner,
                                @NonNull final PageController.Factory factory) {
        super(lifecycleOwner);
        setHasStableIds(true);
        mPageControllerFactory = factory;
        lifecycleOwner.getLifecycle().addObserver(this);
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
        final TractPageBinding binding =
                TractPageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        final PageController controller = PageControllerKt.bindController(binding, mPageControllerFactory);
        binding.setCallbacks(this);
        controller.setCallbacks(this);
        return new RVPageViewHolder(binding);
    }

    @Override
    protected void onBindViewDataBinding(@NonNull final RVPageViewHolder holder,
                                         @NonNull final TractPageBinding binding, final int position) {
        assert mManifest != null;
        final Page page = mManifest.getPages().get(position);
        binding.setPage(page);
        binding.getController().setModel(page);
    }

    @Override
    protected void onUpdatePrimaryItem(@Nullable final RVPageViewHolder old, @Nullable final RVPageViewHolder current) {
        if (current != null && current.getPage() != null) {
            dispatchUpdateActiveCard(current.getPage(), current.getActiveCard());
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
            holder.getBinding().getController().onContentEvent(event);
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void onBroadcastEvent(@NonNull final NavigationEvent event) {
        final RVPageViewHolder holder = getPrimaryItem();
        if (holder != null) {
            holder.getBinding().getController().onLiveShareNavigationEvent(event);
        }
    }

    @Override
    public void onStop(@NonNull final LifecycleOwner owner) {
        Optional.ofNullable(getPrimaryItem())
                .ifPresent(RVPageViewHolder::markHidden);
        EventBus.getDefault().unregister(this);
    }

    // endregion Lifecycle Events

    // region PageController.Callbacks
    @Override
    public void onUpdateActiveCard(@Nullable final Page page, @Nullable final Card card) {
        final RVPageViewHolder holder = getPrimaryItem();
        if (holder != null && page != null && holder.getPage() == page) {
            dispatchUpdateActiveCard(page, card);
        }
    }

    @Override
    public void showModal(@NonNull final Page page, @NonNull final Modal modal) {
        final RVPageViewHolder holder = getPrimaryItem();
        if (mCallbacks != null && holder != null && holder.getPage() == page) {
            mCallbacks.showModal(modal);
        }
    }

    @Override
    public void goToNextPage() {
        if (mCallbacks != null) {
            final RVPageViewHolder holder = getPrimaryItem();
            final int position = holder != null ? holder.getPage().getPosition() + 1 : 0;
            mCallbacks.goToPage(position);
        }
    }
    // endregion PageController.Callbacks

    public static class RVPageViewHolder extends DataBindingViewHolder<TractPageBinding> {
        RVPageViewHolder(@NonNull final TractPageBinding binding) {
            super(binding);
        }

        @Nullable
        public Page getPage() {
            return getBinding().getController().getModel();
        }

        @Nullable
        public Card getActiveCard() {
            return getBinding().getController().getActiveCard();
        }

        void markVisible() {
            getBinding().getController().setVisible(true);
        }

        void markHidden() {
            getBinding().getController().setVisible(false);
        }
    }
}
