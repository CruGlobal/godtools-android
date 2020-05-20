package org.cru.godtools.tract.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.annimon.stream.Stream;
import com.google.android.material.tabs.TabLayout;

import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.analytics.model.AnalyticsDeepLinkEvent;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.tract.analytics.model.ToggleLanguageAnalyticsActionEvent;
import org.cru.godtools.tract.databinding.TractActivityBinding;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public class TractActivity extends KotlinTractActivity
        implements TabLayout.OnTabSelectedListener, GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    static final String EXTRA_LANGUAGES = TractActivity.class.getName() + ".LANGUAGES";
    private static final String EXTRA_ACTIVE_LANGUAGE = TractActivity.class.getName() + ".ACTIVE_LANGUAGE";
    private static final String EXTRA_INITIAL_PAGE = TractActivity.class.getName() + ".INITIAL_PAGE";

    @NonNull
    /*final*/ Locale[] mLanguages = new Locale[0];

    @VisibleForTesting
    int mActiveLanguage = 0;

    // region Lifecycle
    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // read requested tract from the provided intent
        processIntent(getIntent(), savedInstanceState);

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }

        // restore any persisted state
        if (savedInstanceState != null) {
            final Locale activeLanguage = BundleUtils.getLocale(savedInstanceState, EXTRA_ACTIVE_LANGUAGE, null);
            if (activeLanguage != null) {
                updateActiveLanguage(activeLanguage);
            }
            setInitialPage(savedInstanceState.getInt(EXTRA_INITIAL_PAGE, getInitialPage()));
        }

        // track this view
        if (savedInstanceState == null) {
            trackToolOpen(getDataModel().getTool().getValue());
        }

        setupDataModel();
        startLoaders();
        setBinding(TractActivityBinding.inflate(getLayoutInflater()));
        setContentView(getBinding().getRoot());
    }

    @CallSuper
    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        updateLanguageToggle();
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
    protected void onUpdateToolbar() {
        super.onUpdateToolbar();
        updateLanguageToggle();
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        final Locale locale = (Locale) tab.getTag();
        if (locale != null) {
            updateActiveLanguage(locale);
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

    // region Data Model
    private void setupDataModel() {
        getDataModel().setActiveLocale(mLanguages[mActiveLanguage]);
        isInitialSyncFinished().observe(this, finished -> {
            if (finished) { getDataModel().isInitialSyncFinished().setValue(true); }
        });
    }
    // endregion Data Model

    // region Creation Methods
    private void processIntent(@Nullable final Intent intent, @Nullable final Bundle savedInstanceState) {
        final String action = intent != null ? intent.getAction() : null;
        final Uri data = intent != null ? intent.getData() : null;
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (Intent.ACTION_VIEW.equals(action) && isDeepLinkValid(data)) {
            getDataModel().getTool().setValue(extractToolFromDeepLink(data));
            final Pair<List<Locale>, List<Locale>> languages = extractLanguagesFromDeepLink(data);
            getDataModel().getPrimaryLocales().setValue(languages.getFirst());
            getDataModel().getParallelLocales().setValue(languages.getSecond());
            final Integer page = extractPageFromDeepLink(data);
            if (savedInstanceState == null && page != null) {
                setInitialPage(page);
            }

            // track the deep link via analytics only if we aren't re-initializing the Activity w/ savedState
            if (savedInstanceState == null) {
                eventBus.post(new AnalyticsDeepLinkEvent(data));
            }
        } else if (extras != null) {
            getDataModel().getTool().setValue(extras.getString(EXTRA_TOOL, getDataModel().getTool().getValue()));
            final Locale[] raw = BundleUtils.getLocaleArray(extras, EXTRA_LANGUAGES);
            final List<Locale> languages = raw != null ? Arrays.asList(raw) : Collections.emptyList();
            getDataModel().getPrimaryLocales()
                    .setValue(languages.size() > 0 ? languages.subList(0, 1) : Collections.emptyList());
            getDataModel().getParallelLocales()
                    .setValue(languages.size() > 1 ? languages.subList(1, languages.size()) : Collections.emptyList());
        } else {
            getDataModel().getTool().setValue(null);
        }
        mLanguages = CollectionsKt
                .plus(getDataModel().getPrimaryLocales().getValue(), getDataModel().getParallelLocales().getValue())
                .toArray(new Locale[0]);
    }

    private boolean validStartState() {
        return getDataModel().getTool().getValue() != null && mLanguages.length > 0;
    }
    // endregion Creation Methods

    private int determineLanguageState(final int languageIndex) {
        final List<Locale> languages = getDataModel().getLocales().getValue();
        final Map<Locale, Integer> state = getDataModel().getState().getValue();
        return state != null && languages != null && languages.size() > languageIndex ? state.get(languages.get(languageIndex)) : STATE_LOADING;
    }

    @Override
    @CallSuper
    protected void updateVisibilityState() {
        updateActiveLanguageToPotentiallyAvailableLanguageIfNecessary();
        super.updateVisibilityState();
    }

    private void updateActiveLanguage(@NonNull final Locale locale) {
        for (int i = 0; i < mLanguages.length; i++) {
            if (mLanguages[i].equals(locale)) {
                mActiveLanguage = i;
                break;
            }
        }
        getDataModel().setActiveLocale(locale);
    }

    private void updateActiveLanguageToPotentiallyAvailableLanguageIfNecessary() {
        // only process if the active language is not found or invalid
        final int activeLanguageState = determineActiveToolState();
        if (activeLanguageState == STATE_NOT_FOUND || activeLanguageState == STATE_INVALID_TYPE) {
            Stream.of(mLanguages)
                    .filterIndexed((i, l) -> {
                        final int state = determineLanguageState(i);
                        return state != STATE_NOT_FOUND && state != STATE_INVALID_TYPE;
                    })
                    .findFirst()
                    .ifPresent(this::updateActiveLanguage);
        }
    }

    private void updateLanguageToggle() {
        // show or hide the title based on how many visible tabs we have
        if (actionBar != null) {
            final List<Locale> visibleLocales = getDataModel().getVisibleLocales().getValue();
            actionBar.setDisplayShowTitleEnabled(visibleLocales == null || visibleLocales.size() <= 1);
        }
    }

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
            updateLanguageToggle();
        });
        getDataModel().getActiveManifest().observe(this, manifest -> onUpdateActiveManifest());
        getDataModel().getActiveState().observe(this, i -> updateVisibilityState());
    }
}
