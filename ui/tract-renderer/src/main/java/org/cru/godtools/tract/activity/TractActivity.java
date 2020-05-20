package org.cru.godtools.tract.activity;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.tract.analytics.model.ToggleLanguageAnalyticsActionEvent;
import org.cru.godtools.tract.databinding.TractActivityBinding;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import kotlin.collections.CollectionsKt;

public class TractActivity extends KotlinTractActivity
        implements TabLayout.OnTabSelectedListener, GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    private static final String EXTRA_ACTIVE_LANGUAGE = TractActivity.class.getName() + ".ACTIVE_LANGUAGE";
    private static final String EXTRA_INITIAL_PAGE = TractActivity.class.getName() + ".INITIAL_PAGE";

    @NonNull
    /*final*/ Locale[] mLanguages = new Locale[0];

    // region Lifecycle
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isFinishing()) {
            return;
        }

        mLanguages = CollectionsKt
                .plus(getDataModel().getPrimaryLocales().getValue(), getDataModel().getParallelLocales().getValue())
                .toArray(new Locale[0]);

        // restore any persisted state
        if (savedInstanceState != null) {
            final Locale activeLanguage = BundleUtils.getLocale(savedInstanceState, EXTRA_ACTIVE_LANGUAGE, null);
            if (activeLanguage != null) {
                getDataModel().setActiveLocale(activeLanguage);
            }
            setInitialPage(savedInstanceState.getInt(EXTRA_INITIAL_PAGE, getInitialPage()));
        }

        // track this view
        if (savedInstanceState == null) {
            trackToolOpen(getDataModel().getTool().getValue());
        }

        startLoaders();
        setBinding(TractActivityBinding.inflate(getLayoutInflater()));
        setContentView(getBinding().getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventBus.register(this);
    }

    @Override
    @CallSuper
    protected void onUpdateActiveManifest() {
        super.onUpdateActiveManifest();
        showNextFeatureDiscovery();
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        final Locale locale = (Locale) tab.getTag();
        if (locale != null) {
            getDataModel().setActiveLocale(locale);
            eventBus.post(new ToggleLanguageAnalyticsActionEvent(getDataModel().getTool().getValue(), locale));
        }
    }

    @Override
    public void onTabUnselected(final TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(final TabLayout.Tab tab) {
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        checkForPageEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventBus.unregister(this);
        stopDownloadProgressListener();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        BundleUtils.putLocale(outState, EXTRA_ACTIVE_LANGUAGE, getDataModel().getActiveLocale().getValue());
        outState.putInt(EXTRA_INITIAL_PAGE, getInitialPage());
    }
    // endregion Lifecycle

    // region Tool Pager Methods
    private void checkForPageEvent(@NonNull final Event event) {
        final Manifest manifest = getActiveManifest();
        if (manifest != null) {
            for (final Page page : manifest.getPages()) {
                if (page.getListeners().contains(event.id)) {
                    goToPage(page.getPosition());
                }
            }
        }
    }
    // endregion Tool Pager Methods

    private void startLoaders() {
        getDataModel().getState().observe(this, state -> {
            updateVisibilityState();
        });
        getDataModel().getActiveManifest().observe(this, manifest -> onUpdateActiveManifest());
        getDataModel().getActiveState().observe(this, i -> updateVisibilityState());
    }
}
