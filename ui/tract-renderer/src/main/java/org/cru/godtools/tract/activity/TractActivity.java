package org.cru.godtools.tract.activity;

import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.UiThread;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayoutUtils;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.annimon.stream.IntPair;
import com.annimon.stream.Stream;
import com.google.android.instantapps.InstantApps;
import com.google.common.util.concurrent.SettableFuture;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.compat.view.ViewCompat;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.LocaleUtils;
import org.ccci.gto.android.common.util.NumberUtils;
import org.ccci.gto.android.common.util.os.BundleUtils;
import org.cru.godtools.analytics.model.AnalyticsDeepLinkEvent;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.download.manager.DownloadProgress;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.model.Tool;
import org.cru.godtools.model.Translation;
import org.cru.godtools.model.event.ToolUsedEvent;
import org.cru.godtools.model.loader.LatestTranslationLoader;
import org.cru.godtools.sync.task.ToolSyncTasks;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter;
import org.cru.godtools.tract.analytics.model.TractPageAnalyticsScreenEvent;
import org.cru.godtools.tract.content.TractManifestLoader;
import org.cru.godtools.tract.util.DrawableUtils;
import org.cru.godtools.tract.util.ViewUtils;
import org.cru.godtools.tract.viewmodel.ManifestViewUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.cru.godtools.xml.model.Card;
import org.cru.godtools.xml.model.Manifest;
import org.cru.godtools.xml.model.Page;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Contract;
import org.keynote.godtools.android.db.GodToolsDao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;
import static org.cru.godtools.base.Constants.URI_SHARE_BASE;
import static org.cru.godtools.base.ui.util.LocaleTypefaceUtils.safeApplyTypefaceSpan;
import static org.cru.godtools.download.manager.util.ViewUtils.bindDownloadProgress;
import static org.cru.godtools.tract.Constants.PARAM_PARALLEL_LANGUAGE;
import static org.cru.godtools.tract.Constants.PARAM_PRIMARY_LANGUAGE;
import static org.cru.godtools.tract.Constants.PARAM_USE_DEVICE_LANGUAGE;

public class TractActivity extends ImmersiveActivity
        implements ManifestPagerAdapter.Callbacks, TabLayout.OnTabSelectedListener,
        GodToolsDownloadManager.OnDownloadProgressUpdateListener {
    private static final String EXTRA_LANGUAGES = TractActivity.class.getName() + ".LANGUAGES";
    private static final String EXTRA_ACTIVE_LANGUAGE = TractActivity.class.getName() + ".ACTIVE_LANGUAGE";
    private static final String EXTRA_INITIAL_PAGE = TractActivity.class.getName() + ".INITIAL_PAGE";

    private static final int LOADER_ID_BITS = 8;
    private static final int LOADER_ID_MASK = (1 << LOADER_ID_BITS) - 1;
    private static final int LOADER_TYPE_MASK = ~LOADER_ID_MASK;
    private static final int LOADER_TYPE_MANIFEST = 1 << LOADER_ID_BITS;
    private static final int LOADER_TYPE_TRANSLATION = 2 << LOADER_ID_BITS;

    private static final int STATE_LOADING = 0;
    private static final int STATE_LOADED = 1;
    private static final int STATE_NOT_FOUND = 2;

    // App/Action Bar
    @Nullable
    private Menu mToolbarMenu;

    @Nullable
    @BindView(R2.id.language_toggle)
    TabLayout mLanguageTabs;

    // Visibility sections
    @Nullable
    @BindView(R2.id.contentLoading)
    View mLoadingContent;
    @Nullable
    @BindView(R2.id.noContent)
    View mMissingContent;
    @Nullable
    @BindView(R2.id.mainContent)
    View mMainContent;

    @Nullable
    @BindView(R2.id.loading_progress)
    ProgressBar mLoadingProgress;

    // Manifest page pager
    @BindView(R2.id.background_image)
    ScaledPicassoImageView mBackgroundImage;
    @Nullable
    @BindView(R2.id.pages)
    ViewPager mPager;
    @Nullable
    ManifestPagerAdapter mPagerAdapter;

    @Nullable
    /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    /*final*/ Locale[] mLanguages = new Locale[0];
    /*final*/ int mPrimaryLanguages = 1;
    /*final*/ int mParallelLanguages = 0;

    @Nullable
    private GodToolsDownloadManager mDownloadManager;
    @NonNull
    @VisibleForTesting
    SettableFuture[] mDownloadTasks = new SettableFuture[0];
    @NonNull
    private DownloadProgress mDownloadProgress = DownloadProgress.INDETERMINATE;

    private final SparseArray<Translation> mTranslations;
    private final SparseArray<Manifest> mManifests;
    @NonNull
    boolean[] mHiddenLanguages = new boolean[0];
    @VisibleForTesting
    int mActiveLanguage = 0;
    int mInitialPage = 0;

    protected static void populateExtras(@NonNull final Bundle extras, @NonNull final String toolCode,
                                         @NonNull final Locale... languages) {
        extras.putString(EXTRA_TOOL, toolCode);
        // XXX: we use singleString mode to support using this intent for legacy shortcuts
        BundleUtils.putLocaleArray(extras, EXTRA_LANGUAGES, Stream.of(languages).withoutNulls().toArray(Locale[]::new),
                true);
    }

    @NonNull
    public static Intent createIntent(@NonNull final Context context, @NonNull final String toolCode,
                                      @NonNull final Locale... languages) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolCode, languages);
        return new Intent(context, TractActivity.class).putExtras(extras);
    }

    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                             @NonNull final Locale... languages) {
        context.startActivity(createIntent(context, toolCode, languages));
    }

    public TractActivity() {
        mTranslations = new SparseArray<>();
        mManifests = new SparseArray<>();
    }

    @RestrictTo(RestrictTo.Scope.TESTS)
    TractActivity(@NonNull final SparseArray<Translation> translations,
                  @NonNull final SparseArray<Manifest> manifests) {
        mTranslations = translations;
        mManifests = manifests;
    }

    // region Lifecycle Events

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadManager = GodToolsDownloadManager.getInstance(this);

        // read requested tract from the provided intent
        processIntent(getIntent(), savedInstanceState);

        // restore any persisted state
        if (savedInstanceState != null) {
            final Locale activeLanguage = BundleUtils.getLocale(savedInstanceState, EXTRA_ACTIVE_LANGUAGE, null);
            for (int i = 0; i < mLanguages.length; i++) {
                if (mLanguages[i].equals(activeLanguage)) {
                    mActiveLanguage = i;
                    break;
                }
            }
            mInitialPage = savedInstanceState.getInt(EXTRA_INITIAL_PAGE, mInitialPage);
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish();
            return;
        }
        assert mTool != null : "If mTool was null, validStartState() would have failed";

        // download the translations for the requested languages of this tool
        downloadTranslations();

        // track this view
        if (savedInstanceState == null) {
            EventBus.getDefault().post(new ToolUsedEvent(mTool));

            final GodToolsDao dao = GodToolsDao.getInstance(this);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> dao.updateSharesDelta(mTool, 1));
        }

        setContentView(R.layout.activity_tract);
        startLoaders();
        updateVisibilityState();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setupPager();
    }

    @CallSuper
    @Override
    protected void onSetupActionBar() {
        super.onSetupActionBar();
        if (mToolbar != null && InstantApps.isInstantApp(this)) {
            mToolbar.setNavigationIcon(R.drawable.ic_close);
        }
        setupLanguageToggle();
        updateToolbar();
        updateLanguageToggle();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tract, menu);
        mToolbarMenu = menu;

        // make the install menu item visible if this is an Instant App
        final MenuItem install = mToolbarMenu.findItem(R.id.action_install);
        if (install != null) {
            install.setVisible(InstantApps.isInstantApp(this));
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        updateToolbarMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        startDownloadProgressListener();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_share) {
            shareCurrentTract();
            return true;
        } else if (id == R.id.action_install) {
            installFullAppFromInstantApp();
            return true;
        } else if (id == android.R.id.home) {
            // handle close button if this is an instant app
            if (InstantApps.isInstantApp(this)) {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDownloadProgressUpdated(@Nullable final DownloadProgress progress) {
        mDownloadProgress = progress != null ? progress : DownloadProgress.INDETERMINATE;
        bindDownloadProgress(mLoadingProgress, mDownloadProgress);
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        final Locale locale = (Locale) tab.getTag();
        if (locale != null) {
            updateActiveLanguage(locale);
            mAnalytics.onTrackToggleLanguage(locale);
        }
    }

    @Override
    public void onTabUnselected(final TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(final TabLayout.Tab tab) {
    }

    @Override
    public void onUpdateActiveCard(@NonNull final Page page,
                                   @Nullable final Card card) {
        trackTractPage(page, card);
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        checkForPageEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        stopDownloadProgressListener();
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActiveLanguage < mLanguages.length) {
            BundleUtils.putLocale(outState, EXTRA_ACTIVE_LANGUAGE, mLanguages[mActiveLanguage]);
        }
        outState.putInt(EXTRA_INITIAL_PAGE, mInitialPage);
    }

    // endregion Lifecycle Events

    // region Creation Methods

    private void processIntent(@Nullable final Intent intent, @Nullable final Bundle savedInstanceState) {
        final String action = intent != null ? intent.getAction() : null;
        final Uri data = intent != null ? intent.getData() : null;
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (Intent.ACTION_VIEW.equals(action) && isDeepLinkValid(data)) {
            mTool = getToolFromDeepLink(data);
            mLanguages = processDeepLinkLanguages(data);
            processDeepLinkPage(data, savedInstanceState);

            // track the deep link via analytics only if we aren't re-initializing the Activity w/ savedState
            if (savedInstanceState == null) {
                EventBus.getDefault().post(new AnalyticsDeepLinkEvent(data));
            }
        } else if (extras != null) {
            mTool = extras.getString(EXTRA_TOOL, mTool);
            final Locale[] languages = BundleUtils.getLocaleArray(extras, EXTRA_LANGUAGES);
            mLanguages = languages != null ? languages : mLanguages;
        }
        mHiddenLanguages = new boolean[mLanguages.length];

        mDownloadTasks = new SettableFuture[mLanguages.length];
    }

    @Contract("null -> false")
    private boolean isDeepLinkValid(@Nullable final Uri data) {
        if (data != null) {
            if ("http".equalsIgnoreCase(data.getScheme()) || "https".equalsIgnoreCase(data.getScheme())) {
                final String host1 = getString(R.string.tract_deeplink_host_1);
                final String host2 = getString(R.string.tract_deeplink_host_2);
                if (host1.equalsIgnoreCase(data.getHost()) || host2.equalsIgnoreCase(data.getHost())) {
                    return data.getPathSegments().size() >= 2;
                }
            }
        }
        return false;
    }

    @NonNull
    private Locale[] processDeepLinkLanguages(@NonNull final Uri data) {
        final List<Locale> locales = new ArrayList<>();

        // process the primary languages specified in the uri
        final List<Locale> rawPrimaryLanguages = new ArrayList<>();
        if (!TextUtils.isEmpty(data.getQueryParameter(PARAM_USE_DEVICE_LANGUAGE))) {
            rawPrimaryLanguages.add(Locale.getDefault());
        }
        streamLanguageParamater(data, PARAM_PRIMARY_LANGUAGE).forEach(rawPrimaryLanguages::add);
        rawPrimaryLanguages.add(LocaleCompat.forLanguageTag(data.getPathSegments().get(0)));
        final Locale[] primaryLanguages = LocaleUtils.getFallbacks(rawPrimaryLanguages.toArray(new Locale[0]));
        Collections.addAll(locales, primaryLanguages);
        mPrimaryLanguages = primaryLanguages.length;

        // process parallel languages specified in the uri
        final Locale[] parallelLanguages = LocaleUtils.getFallbacks(
                streamLanguageParamater(data, PARAM_PARALLEL_LANGUAGE).toArray(Locale[]::new));
        Collections.addAll(locales, parallelLanguages);
        mParallelLanguages = parallelLanguages.length;

        // return all the parsed languages
        return locales.toArray(new Locale[0]);
    }

    @NonNull
    private Stream<Locale> streamLanguageParamater(@NonNull final Uri data, @NonNull final String param) {
        return Stream.of(data.getQueryParameters(param))
                .flatMap(lang -> Stream.of(TextUtils.split(lang, ",")))
                .map(String::trim)
                .filterNot(TextUtils::isEmpty)
                .map(LocaleCompat::forLanguageTag);
    }

    private void processDeepLinkPage(@NonNull final Uri data, @Nullable final Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            final List<String> segments = data.getPathSegments();
            if (segments.size() >= 3) {
                mInitialPage = NumberUtils.toInteger(segments.get(2), mInitialPage);
            }
        }
    }

    @Nullable
    private String getToolFromDeepLink(@NonNull final Uri data) {
        return data.getPathSegments().get(1);
    }

    private boolean validStartState() {
        return mTool != null && mLanguages.length > 0;
    }

    // endregion Creation Methods

    private void downloadTranslations() {
        if (mDownloadManager != null && mTool != null) {
            for (int language = 0; language < mLanguages.length; language++) {
                mDownloadManager.cacheTranslation(mTool, mLanguages[language]);
                mDownloadTasks[language] = SettableFuture.create();
                AsyncTask.THREAD_POOL_EXECUTOR.execute(
                        new DownloadTranslationRunnable(this, mTool, mLanguages[language], mDownloadTasks[language]));
            }
        }
    }

    private int determineLanguageState(final int languageIndex) {
        if (mManifests.get(languageIndex) != null) {
            return STATE_LOADED;
        } else if (mDownloadTasks[languageIndex] != null && mDownloadTasks[languageIndex].isDone() &&
                mTranslations.indexOfKey(languageIndex) >= 0 && mTranslations.get(languageIndex) == null) {
            return STATE_NOT_FOUND;
        } else {
            return STATE_LOADING;
        }
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
            mHiddenLanguages[i] = state == STATE_NOT_FOUND;

            // short-circuit loop if this language was not found
            if (state == STATE_NOT_FOUND) {
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

    private void updateVisibilityState() {
        updateActiveLanguageToPotentiallyAvailableLanguageIfNecessary();
        final int state = determineLanguageState(mActiveLanguage);

        if (mLoadingContent != null) {
            mLoadingContent.setVisibility(state == STATE_LOADING ? View.VISIBLE : View.GONE);
        }
        if (mMainContent != null) {
            mMainContent.setVisibility(state == STATE_LOADED ? View.VISIBLE : View.GONE);
        }
        if (mMissingContent != null) {
            mMissingContent.setVisibility(state == STATE_NOT_FOUND ? View.VISIBLE : View.GONE);
        }
    }

    private void setupLanguageToggle() {
        if (mLanguageTabs != null) {
            ViewCompat.setClipToOutline(mLanguageTabs, true);

            for (final Locale locale : mLanguages) {
                mLanguageTabs.addTab(mLanguageTabs.newTab()
                        .setText(locale.getDisplayName(locale))
                        .setTag(locale));
            }

            mLanguageTabs.addOnTabSelectedListener(this);
        }
    }

    void setTranslation(@NonNull final Locale locale, @Nullable final Translation translation) {
        for (int i = 0; i < mLanguages.length; i++) {
            if (locale.equals(mLanguages[i])) {
                mTranslations.put(i, translation);

                if (i == mActiveLanguage) {
                    updateVisibilityState();
                }
                break;
            }
        }
    }

    void setManifest(@NonNull final Locale locale, @Nullable final Manifest manifest) {
        for (int i = 0; i < mLanguages.length; i++) {
            if (locale.equals(mLanguages[i])) {
                if (manifest != null) {
                    mManifests.put(i, manifest);
                } else {
                    mManifests.remove(i);
                }

                if (i == mActiveLanguage) {
                    updateActiveManifest();
                    updateVisibilityState();
                }
                updateLanguageToggle();
                break;
            }
        }
    }

    private void updateActiveLanguage(@NonNull final Locale locale) {
        for (int i = 0; i < mLanguages.length; i++) {
            if (mLanguages[i].equals(locale)) {
                if (i != mActiveLanguage) {
                    mActiveLanguage = i;
                    restartDownloadProgressListener();
                    updateActiveManifest();
                    updateLanguageToggle();
                }
                return;
            }
        }
    }

    private void updateActiveLanguageToPotentiallyAvailableLanguageIfNecessary() {
        // only process if the active language is not found
        if (determineLanguageState(mActiveLanguage) == STATE_NOT_FOUND) {
            Stream.of(mLanguages).indexed()
                    .filter(l -> determineLanguageState(l.getFirst()) != STATE_NOT_FOUND)
                    .map(IntPair::getSecond)
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

            // skip any language that we don't have a manifest for
            if (mManifests.get(i) == null) {
                continue;
            }

            // return this language
            return mLanguages[i];
        }

        // default to null
        return null;
    }

    @Nullable
    private Manifest getActiveManifest() {
        return mManifests.get(mActiveLanguage);
    }

    private void updateActiveManifest() {
        updateToolbar();
        updateBackground();
        updatePager();
    }

    private void updateToolbar() {
        final Manifest manifest = getActiveManifest();
        setTitle(safeApplyTypefaceSpan(Manifest.getTitle(manifest), ManifestViewUtils.getTypeface(manifest, this)));

        if (mToolbar != null) {
            // set toolbar background color
            mToolbar.setBackgroundColor(Manifest.getNavBarColor(manifest));

            // set text & controls color
            final int controlColor = Manifest.getNavBarControlColor(manifest);
            mToolbar.setNavigationIcon(DrawableUtils.tint(mToolbar.getNavigationIcon(), controlColor));
            mToolbar.setTitleTextColor(controlColor);
            mToolbar.setSubtitleTextColor(controlColor);
        }

        updateToolbarMenu();
        updateLanguageToggle();
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
                    final boolean visible = mManifests.get(i) != null && !mHiddenLanguages[i];
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
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(visibleTabs <= 1);
        }
    }

    private void updateToolbarMenu() {
        if (mToolbarMenu != null) {
            // tint all action icons
            final int controlColor = Manifest.getNavBarControlColor(getActiveManifest());
            for (int i = 0; i < mToolbarMenu.size(); ++i) {
                final MenuItem item = mToolbarMenu.getItem(i);
                item.setIcon(DrawableUtils.tint(item.getIcon(), controlColor));
            }
            if (mToolbar != null) {
                mToolbar.setOverflowIcon(DrawableUtils.tint(mToolbar.getOverflowIcon(), controlColor));
            }
        }
    }

    private void updateBackground() {
        final Manifest manifest = getActiveManifest();
        getWindow().getDecorView().setBackgroundColor(Manifest.getBackgroundColor(manifest));
        ManifestViewUtils.bindBackgroundImage(manifest, mBackgroundImage);
    }

    // region Tool Pager Methods

    private void setupPager() {
        if (mPager != null) {
            mPagerAdapter = new ManifestPagerAdapter();
            mPagerAdapter.setCallbacks(this);
            mPager.setAdapter(mPagerAdapter);
            getLifecycle().addObserver(mPagerAdapter);
            updatePager();
        }
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

    @Override
    public void goToPage(final int position) {
        if (mPager != null) {
            mPager.setCurrentItem(position);
        }
    }

    private void updatePager() {
        if (mPagerAdapter != null) {
            final Manifest manifest = getActiveManifest();
            mPagerAdapter.setManifest(manifest);

            if (mPager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mPager.setLayoutDirection(TextUtils.getLayoutDirectionFromLocale(getFirstVisibleLocale()));
                }

                // scroll to initial page
                if (manifest != null && mInitialPage >= 0) {
                    mPager.setCurrentItem(mInitialPage, false);
                    mInitialPage = -1;
                }
            }
        }
    }

    // endregion Tool Pager Methods

    private void startLoaders() {
        final LoaderManager manager = getSupportLoaderManager();

        final ManifestLoaderCallbacks manifestCallbacks = new ManifestLoaderCallbacks();
        final TranslationLoaderCallbacks translationLoaderCallbacks = new TranslationLoaderCallbacks();
        for (int i = 0; i < mLanguages.length; i++) {
            manager.initLoader(LOADER_TYPE_MANIFEST + i, null, manifestCallbacks);
            manager.initLoader(LOADER_TYPE_TRANSLATION + i, null, translationLoaderCallbacks);
        }
    }

    private void startDownloadProgressListener() {
        if (mDownloadManager != null && mTool != null) {
            mDownloadManager.addOnDownloadProgressUpdateListener(mTool, mLanguages[mActiveLanguage], this);
            onDownloadProgressUpdated(mDownloadManager.getDownloadProgress(mTool, mLanguages[mActiveLanguage]));
        }
    }

    private void restartDownloadProgressListener() {
        stopDownloadProgressListener();
        if (getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            startDownloadProgressListener();
        }
    }

    private void stopDownloadProgressListener() {
        if (mDownloadManager != null) {
            mDownloadManager.removeOnDownloadProgressUpdateListener(this);
        }
    }

    private void installFullAppFromInstantApp() {
        InstantApps.showInstallPrompt(this, null, -1, "instantapp");
    }

    private void shareCurrentTract() {
        final Manifest manifest = getActiveManifest();
        if (manifest != null) {
            mAnalytics.onTrackShareAction();

            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tract_subject, manifest.getTitle()));
            intent.putExtra(Intent.EXTRA_TEXT, buildSharingURL(manifest, mPager != null ? mPager.getCurrentItem() : 0));
            startActivity(Intent.createChooser(intent, getString(R.string.share_tract_title, manifest.getTitle())));
        }
    }

    /**
     * This method takes in a Manifest and page and returns a shareable link for the corresponding tool.
     *
     * @param manifest The Manifest Object to be used.
     * @param page     The page being shared
     * @return Url string of the shareable link.
     */
    @NonNull
    private String buildSharingURL(@NonNull final Manifest manifest, final int page) {
        final Uri.Builder uri = URI_SHARE_BASE.buildUpon()
                .appendEncodedPath(LocaleCompat.toLanguageTag(manifest.getLocale()).toLowerCase())
                .appendPath(manifest.getCode());
        if (page > 0) {
            uri.appendPath(String.valueOf(page));
        }

        return uri
                .appendQueryParameter("icid", "gtshare")
                .build().toString();
    }

    void trackTractPage(@NonNull final Page page, @Nullable final Card card) {
        final Manifest manifest = page.getManifest();
        mEventBus.post(new TractPageAnalyticsScreenEvent(manifest.getCode(), manifest.getLocale(), page.getPosition(),
                card != null ? card.getPosition() : null));
    }

    class TranslationLoaderCallbacks implements LoaderManager.LoaderCallbacks<Translation> {
        @Nullable
        @Override
        public Loader<Translation> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id & LOADER_TYPE_MASK) {
                case LOADER_TYPE_TRANSLATION:
                    final int langId = id & LOADER_ID_MASK;
                    if (mTool != null && langId >= 0 && langId < mLanguages.length) {
                        return new LatestTranslationLoader(TractActivity.this, mTool, mLanguages[langId]);
                    }
                    break;
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Translation> loader, @Nullable final Translation translation) {
            switch (loader.getId() & LOADER_TYPE_MASK) {
                case LOADER_TYPE_TRANSLATION:
                    if (loader instanceof LatestTranslationLoader) {
                        setTranslation(((LatestTranslationLoader) loader).getLocale(), translation);
                    }
                    break;
            }
        }

        @Override
        public void onLoaderReset(@NonNull final Loader<Translation> loader) {
            // noop
        }
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            switch (id & LOADER_TYPE_MASK) {
                case LOADER_TYPE_MANIFEST:
                    final int langId = id & LOADER_ID_MASK;
                    if (mTool != null && langId >= 0 && langId < mLanguages.length) {
                        return new TractManifestLoader(TractActivity.this, mTool, mLanguages[langId]);
                    }
                    break;
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Manifest> loader, @Nullable final Manifest manifest) {
            switch (loader.getId() & LOADER_TYPE_MASK) {
                case LOADER_TYPE_MANIFEST:
                    if (loader instanceof TractManifestLoader) {
                        setManifest(((TractManifestLoader) loader).getLocale(), manifest);
                    }
                    break;
            }
        }
    }

    static class DownloadTranslationRunnable implements Runnable {
        private final Context mContext;
        private final String mTool;
        private final Locale mLocale;
        private final SettableFuture<?> mFuture;

        DownloadTranslationRunnable(@NonNull final Context context, @NonNull final String tool,
                                    @NonNull final Locale locale, @NonNull final SettableFuture<?> future) {
            mContext = context.getApplicationContext();
            mTool = tool;
            mLocale = locale;
            mFuture = future;
        }

        @Override
        public void run() {
            try {
                new ToolSyncTasks(mContext).syncTools(Bundle.EMPTY);
            } catch (final IOException ignored) {
            }
            GodToolsDownloadManager.getInstance(mContext).cacheTranslation(mTool, mLocale);
            mFuture.set(null);
        }
    }
}
