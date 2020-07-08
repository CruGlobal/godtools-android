package org.cru.godtools.tract.viewmodel;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.android.material.tabs.TabLayout;

import org.ccci.gto.android.common.material.tabs.TabLayoutKt;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.tract.databinding.TractContentTabsBinding;
import org.cru.godtools.tract.ui.controller.TabController;
import org.cru.godtools.tract.ui.controller.UiControllerCache;
import org.cru.godtools.xml.model.StylesKt;
import org.cru.godtools.xml.model.Tab;
import org.cru.godtools.xml.model.Tabs;
import org.cru.godtools.xml.model.Text;
import org.cru.godtools.xml.model.TextKt;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

@UiThread
public final class TabsViewHolder extends BaseViewHolder<Tabs> implements TabLayout.OnTabSelectedListener {
    private static final TabController[] EMPTY_TAB_VIEW_HOLDERS = new TabController[0];

    private final TractContentTabsBinding mBinding;

    private boolean mBindingTabs = false;

    @NonNull
    private TabController[] mTabContentViewHolders = EMPTY_TAB_VIEW_HOLDERS;
    private final UiControllerCache mTabCache;

    private TabsViewHolder(@NonNull final TractContentTabsBinding binding,
                           @Nullable final BaseViewHolder parentViewHolder) {
        super(Tabs.class, binding.getRoot(), parentViewHolder);
        mBinding = binding;
        mTabCache = new UiControllerCache(binding.tab, this);
        setupTabs();
    }

    public static TabsViewHolder create(@NonNull final ViewGroup parent,
                                        @Nullable final BaseViewHolder parentViewHolder) {
        return new TabsViewHolder(
                TractContentTabsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false),
                parentViewHolder);
    }

    // region Lifecycle Events

    @UiThread
    @Override
    protected void onBind() {
        super.onBind();
        mBinding.setModel(getModel());
        bindTabs();
    }

    @Override
    @CallSuper
    public void onContentEvent(@NonNull final Event event) {
        super.onContentEvent(event);
        checkForTabEvent(event);
        propagateEventToChildren(event);
    }

    @Override
    public void onTabSelected(@NonNull final TabLayout.Tab tab) {
        final TabController holder = showTabContent(tab.getPosition());
        if (!mBindingTabs) {
            holder.trackSelectedAnalyticsEvents();
        }
    }

    @Override
    public void onTabUnselected(final TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(final TabLayout.Tab tab) {}

    // endregion Lifecycle Events

    private void setupTabs() {
        mBinding.tabs.addOnTabSelectedListener(this);
    }

    private void bindTabs() {
        mBindingTabs = true;

        // remove all the old tabs
        mBinding.tabs.removeAllTabs();
        Stream.of(mTabContentViewHolders)
                .peek(vh -> mBinding.tab.removeView(vh.mRoot))
                .peek(vh -> vh.bind(null))
                .forEach(c -> c.releaseTo(mTabCache));
        mTabContentViewHolders = EMPTY_TAB_VIEW_HOLDERS;

        // add all the current tabs
        if (mModel != null) {
            // create view holders for every tab
            mTabContentViewHolders = Stream.of(mModel.getTabs())
                    .map(this::bindTabContentViewHolder)
                    .toArray(TabController[]::new);

            // add all the tabs to the TabLayout
            final int primaryColor = StylesKt.getPrimaryColor(mModel.getStylesParent());
            for (final Tab tab : mModel.getTabs()) {
                final Text label = tab.getLabel();
                final TabLayout.Tab tab2 = mBinding.tabs.newTab()
                        .setText(TextKt.getText(label));

                // set the tab background
                TabLayoutKt.setBackgroundTint(tab2, primaryColor);

                mBinding.tabs.addTab(tab2);
            }
        }

        mBindingTabs = false;
    }

    private void selectTab(@NonNull final Tab tab) {
        Optional.ofNullable(mBinding.tabs.getTabAt(tab.getPosition()))
                .ifPresent(TabLayout.Tab::select);
    }

    @NonNull
    private TabController bindTabContentViewHolder(@Nullable final Tab tab) {
        final TabController controller = (TabController) mTabCache.acquire(Tab.class);
        controller.bind(tab);
        return controller;
    }

    private TabController showTabContent(final int position) {
        final TabController holder = mTabContentViewHolders[position];
        if (holder.mRoot.getParent() != mBinding.tab) {
            mBinding.tab.removeAllViews();
            mBinding.tab.addView(holder.mRoot);
        }
        return holder;
    }

    private void propagateEventToChildren(@NonNull final Event event) {
        final int position = mBinding.tabs.getSelectedTabPosition();
        if (position > -1) {
            mTabContentViewHolders[position].onContentEvent(event);
        }
    }

    private void checkForTabEvent(@NonNull final Event event) {
        // check for card display event
        if (mModel != null) {
            Stream.of(mModel.getTabs())
                    .filter(c -> c.getListeners().contains(event.id))
                    .findFirst()
                    .ifPresent(this::selectTab);
        }
    }
}
