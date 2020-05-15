package org.cru.godtools.tract.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.annimon.stream.Stream;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutUtils;

import org.ccci.gto.android.common.compat.view.ViewCompat;
import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.analytics.model.AnalyticsDeepLinkEvent;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.base.util.LocaleUtils;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.analytics.model.ToggleLanguageAnalyticsActionEvent;
import org.cru.godtools.tract.databinding.TractActivityBinding;
import org.cru.godtools.tract.util.ViewUtils;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.annotation.VisibleForTesting;
import butterknife.BindView;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;

public class TractActivity extends KotlinTractActivity
        implements TabLayout.OnTabSelectedListener, GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    static final String EXTRA_LANGUAGES = TractActivity.class.getName() + ".LANGUAGES";
    private static final String EXTRA_ACTIVE_LANGUAGE = TractActivity.class.getName() + ".ACTIVE_LANGUAGE";
    private static final String EXTRA_INITIAL_PAGE = TractActivity.class.getName() + ".INITIAL_PAGE";

    @Nullable
    @BindView(R2.id.language_toggle)
    TabLayout mLanguageTabs;

    @NonNull
    /*final*/ Locale[] mLanguages = new Locale[0];
    /*final*/ int mPrimaryLanguages = 1;
    /*final*/ int mParallelLanguages = 0;

    @NonNull
    boolean[] mHiddenLanguages = new boolean[0];
    @VisibleForTesting
    int mActiveLanguage = 0;

    public TractActivity() {
        super();
    }

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
            for (int i = 0; i < mLanguages.length; i++) {
                if (mLanguages[i].equals(activeLanguage)) {
                    mActiveLanguage = i;
                    break;
                }
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

    @Override
    @CallSuper
    public void onContentChanged() {
        super.onContentChanged();
        setupPager();
    }

    @CallSuper
    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        setupLanguageToggle();
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
        updatePager();
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
        isSyncRunning().observe(this, running -> getDataModel().isSyncRunning().setValue(running));
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
        mPrimaryLanguages = getDataModel().getPrimaryLocales().getValue().size();
        mParallelLanguages = getDataModel().getParallelLocales().getValue().size();
        mLanguages = CollectionsKt
                .plus(getDataModel().getPrimaryLocales().getValue(), getDataModel().getParallelLocales().getValue())
                .toArray(new Locale[0]);
        mHiddenLanguages = new boolean[mLanguages.length];
    }

    private boolean validStartState() {
        return getDataModel().getTool().getValue() != null && mLanguages.length > 0;
    }
    // endregion Creation Methods

    @Override
    protected int determineActiveToolState() {
        final Integer state = getDataModel().getActiveState().getValue();
        return state != null ? state : STATE_LOADING;
    }

    private int determineLanguageState(final int languageIndex) {
        final List<Integer> state = getDataModel().getState().getValue();
        return state != null && state.size() > languageIndex ? state.get(languageIndex) : STATE_LOADING;
    }

    /**
     * This method updates the list of artificially hidden languages. This includes primary language fallbacks provided
     * via a deep link.
     */
    @UiThread
    @VisibleForTesting
    void updateHiddenLanguages() {
        showSingleBestLanguageInRange(0, mPrimaryLanguages);
        showSingleBestLanguageInRange(mPrimaryLanguages, mPrimaryLanguages + mParallelLanguages);
    }

    private void showSingleBestLanguageInRange(final int start, final int end) {
        int language = -1;
        for (int i = start; i < mLanguages.length && i < end; i++) {
            // default hidden state to whether the language exists or not
            final int state = determineLanguageState(i);
            mHiddenLanguages[i] = state == STATE_NOT_FOUND || state == STATE_INVALID_TYPE;

            // short-circuit loop if this language was not found
            if (mHiddenLanguages[i]) {
                continue;
            }

            // is this language currently active, or is it loaded and we haven't found a language to show yet
            if (mActiveLanguage == i || (state == STATE_LOADED && language == -1)) {
                // don't hide the language
                mHiddenLanguages[i] = false;

                // hide any previously identified languages
                if (language != -1) {
                    mHiddenLanguages[language] = true;
                }

                // track our current language
                language = i;
            } else {
                // hide any other potential language
                mHiddenLanguages[i] = true;
            }
        }
    }

    @Override
    @CallSuper
    protected void updateVisibilityState() {
        updateActiveLanguageToPotentiallyAvailableLanguageIfNecessary();
        super.updateVisibilityState();
    }

    private void setupLanguageToggle() {
        if (mLanguageTabs != null) {
            ViewCompat.setClipToOutline(mLanguageTabs, true);

            for (final Locale locale : mLanguages) {
                mLanguageTabs.addTab(mLanguageTabs.newTab()
                                             .setText(LocaleUtils.getDisplayName(locale, mLanguageTabs.getContext(),
                                                                                 null, locale))
                                             .setTag(locale));
            }

            mLanguageTabs.addOnTabSelectedListener(this);
        }
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

    @Nullable
    private Locale getFirstVisibleLocale() {
        for (int i = 0; i < mLanguages.length; i++) {
            // skip any hidden languages
            if (mHiddenLanguages[i]) {
                continue;
            }

            // skip any language that isn't loaded
            if (determineLanguageState(i) != STATE_LOADED) {
                continue;
            }

            // return this language
            return mLanguages[i];
        }

        // default to null
        return null;
    }

    private void updateLanguageToggle() {
        // update the styles for the language tabs
        int visibleTabs = 0;
        if (mLanguageTabs != null) {
            // determine colors for the language toggle
            final Manifest manifest = getActiveManifest();
            final int controlColor = Manifest.getNavBarControlColor(manifest);
            int selectedColor = Manifest.getNavBarColor(manifest);
            if (Color.alpha(selectedColor) < 255) {
                // XXX: the expected behavior is to support transparent text. But we currently don't support
                // XXX: transparent text, so pick white or black based on the control color
                final float[] hsv = new float[3];
                Color.colorToHSV(controlColor, hsv);
                selectedColor = hsv[2] > 0.6 ? Color.BLACK : Color.WHITE;
            }

            // update colors for tab text, and background
            mLanguageTabs.setTabTextColors(controlColor, selectedColor);
            ViewUtils.setBackgroundTint(mLanguageTabs, controlColor);

            // update visible tabs
            updateHiddenLanguages();
            for (int i = 0; i < mLanguages.length; i++) {
                final TabLayout.Tab tab = mLanguageTabs.getTabAt(i);
                if (tab != null) {
                    // update tab visibility
                    final boolean visible = !mHiddenLanguages[i] && determineLanguageState(i) == STATE_LOADED;
                    TabLayoutUtils.setVisibility(tab, visible ? View.VISIBLE : View.GONE);
                    if (visible) {
                        visibleTabs++;
                    }

                    // update tab background
                    TabLayoutUtils.setBackgroundTint(tab, controlColor);

                    // ensure tab is selected if it is active
                    if (i == mActiveLanguage && !tab.isSelected()) {
                        tab.select();
                    }
                }
            }

            mLanguageTabs.setVisibility(visibleTabs > 1 ? View.VISIBLE : View.GONE);
        }

        // show or hide the title based on how many visible tabs we have
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(visibleTabs <= 1);
        }
    }

    // region Tool Pager Methods
    private void setupPager() {
        // TODO: this needs to be triggered on hidden tool updates as well
        getDataModel().getState().observe(this, state -> updatePager());
    }

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

    private void updatePager() {
        getPager().setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(getFirstVisibleLocale()));
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
