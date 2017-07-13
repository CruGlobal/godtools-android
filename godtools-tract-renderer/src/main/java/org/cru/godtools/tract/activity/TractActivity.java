package org.cru.godtools.tract.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayoutUtils;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.annimon.stream.Stream;

import org.ccci.gto.android.common.compat.util.LocaleCompat;
import org.ccci.gto.android.common.support.v4.app.SimpleLoaderCallbacks;
import org.ccci.gto.android.common.util.BundleUtils;
import org.cru.godtools.base.model.Event;
import org.cru.godtools.download.manager.GodToolsDownloadManager;
import org.cru.godtools.tract.R;
import org.cru.godtools.tract.R2;
import org.cru.godtools.tract.adapter.ManifestPagerAdapter;
import org.cru.godtools.tract.content.TractManifestLoader;
import org.cru.godtools.tract.model.Manifest;
import org.cru.godtools.tract.model.Page;
import org.cru.godtools.tract.util.DrawableUtils;
import org.cru.godtools.tract.util.ViewUtils;
import org.cru.godtools.tract.widget.ScaledPicassoImageView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.Contract;
import org.keynote.godtools.android.db.GodToolsDao;
import org.keynote.godtools.android.model.Tool;

import java.util.Locale;

import butterknife.BindView;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

import static org.cru.godtools.base.Constants.EXTRA_TOOL;
import static org.cru.godtools.base.Constants.URI_SHARE_BASE;

public class TractActivity extends ImmersiveActivity
        implements ManifestPagerAdapter.Callbacks, TabLayout.OnTabSelectedListener {
    private static final String EXTRA_LANGUAGES = TractActivity.class.getName() + ".LANGUAGES";
    private static final String EXTRA_ACTIVE_LANGUAGE = TractActivity.class.getName() + ".ACTIVE_LANGUAGE";

    private static final int LOADER_MANIFEST_BASE = 1 << 15;

    // App/Action Bar
    @BindView(R2.id.appBar)
    Toolbar mToolbar;
    @Nullable
    private Menu mToolbarMenu;
    @Nullable
    private ActionBar mActionBar;

    @Nullable
    @BindView(R2.id.language_toggle)
    TabLayout mLanguageTabs;
    @BindView(R2.id.background_image)
    ScaledPicassoImageView mBackgroundImage;

    // Manifest page pager
    @Nullable
    @BindView(R2.id.pages)
    ViewPager mPager;
    @Nullable
    ManifestPagerAdapter mPagerAdapter;

    @Nullable
    /*final*/ String mTool = Tool.INVALID_CODE;
    @NonNull
    /*final*/ Locale[] mLanguages = new Locale[0];

    private final SparseArray<Manifest> mManifests = new SparseArray<>();
    private int mActiveLanguage = 0;

    protected static void populateExtras(@NonNull final Bundle extras, @NonNull final String toolCode,
                                         @NonNull final Locale... languages) {
        extras.putString(EXTRA_TOOL, toolCode);
        BundleUtils.putLocaleArray(extras, EXTRA_LANGUAGES, Stream.of(languages).withoutNulls().toArray(Locale[]::new));
    }

    public static void start(@NonNull final Context context, @NonNull final String toolCode,
                             @NonNull final Locale... languages) {
        final Bundle extras = new Bundle();
        populateExtras(extras, toolCode, languages);
        context.startActivity(new Intent(context, TractActivity.class).putExtras(extras));
    }

    /* BEGIN lifecycle */

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // read requested tract from the provided intent
        final Intent intent = getIntent();
        final String action = intent != null ? intent.getAction() : null;
        final Uri data = intent != null ? intent.getData() : null;
        final Bundle extras = intent != null ? intent.getExtras() : null;
        if (Intent.ACTION_VIEW.equals(action) && isDeepLinkValid(data)) {
            mTool = getToolFromDeepLink(data);
            final Locale language = getLanguageFromDeepLink(data);
            mLanguages = new Locale[] {language};
        } else if (extras != null) {
            mTool = extras.getString(EXTRA_TOOL, mTool);
            final Locale[] languages = BundleUtils.getLocaleArray(extras, EXTRA_LANGUAGES);
            mLanguages = languages != null ? languages : mLanguages;
        }

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
        }

        // cache the translation for the active language of this tool
        assert mTool != null;
        GodToolsDownloadManager.getInstance(this).cacheTranslation(mTool, mLanguages[mActiveLanguage]);

        // track this share
        if (savedInstanceState == null) {
            final GodToolsDao dao = GodToolsDao.getInstance(this);
            AsyncTask.THREAD_POOL_EXECUTOR.execute(() -> dao.updateSharesDelta(mTool, 1));
        }

        setContentView(R.layout.activity_tract);
        startLoaders();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        setupToolbar();
        setupPager();
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull final Menu menu) {
        getMenuInflater().inflate(R.menu.activity_tract, menu);
        mToolbarMenu = menu;
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
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_share) {
            shareCurrentTract();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab) {
        final Locale locale = (Locale) tab.getTag();
        for (int i = 0; i < mLanguages.length; i++) {
            if (mLanguages[i].equals(locale)) {
                mActiveLanguage = i;
                updateActiveManifest();
                return;
            }
        }
    }

    @Override
    public void onTabUnselected(final TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(final TabLayout.Tab tab) {}

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onContentEvent(@NonNull final Event event) {
        checkForPageEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActiveLanguage < mLanguages.length) {
            BundleUtils.putLocale(outState, EXTRA_ACTIVE_LANGUAGE, mLanguages[mActiveLanguage]);
        }
    }

    /* END lifecycle */

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
    private Locale getLanguageFromDeepLink(@NonNull final Uri data) {
        return LocaleCompat.forLanguageTag(data.getPathSegments().get(0));
    }

    @Nullable
    private String getToolFromDeepLink(@NonNull final Uri data) {
        return data.getPathSegments().get(1);
    }

    private boolean validStartState() {
        return mTool != null && mLanguages.length > 0;
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        setupLanguageToggle();
        updateToolbar();
        updateLanguageToggle();
    }

    private void setupLanguageToggle() {
        if (mLanguageTabs != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mLanguageTabs.setClipToOutline(true);
            }

            for (final Locale locale : mLanguages) {
                mLanguageTabs.addTab(mLanguageTabs.newTab()
                                             .setText(locale.getDisplayName())
                                             .setTag(locale));
            }

            mLanguageTabs.addOnTabSelectedListener(this);
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
                }
                updateLanguageToggle();
                break;
            }
        }
    }

    @Nullable
    protected Manifest getActiveManifest() {
        return mManifests.get(mActiveLanguage);
    }

    private void updateActiveManifest() {
        updateToolbar();
        updateBackground();
        updatePager();
    }

    private void updateToolbar() {
        final Manifest manifest = getActiveManifest();
        setTitle(CalligraphyUtils.applyTypefaceSpan(Manifest.getTitle(manifest), Manifest.getTypeface(manifest, this)));

        // set toolbar background color
        mToolbar.setBackgroundColor(Manifest.getNavBarColor(manifest));

        // set text & controls color
        final int controlColor = Manifest.getNavBarControlColor(manifest);
        mToolbar.setTitleTextColor(controlColor);
        mToolbar.setSubtitleTextColor(controlColor);
        mToolbar.setNavigationIcon(DrawableUtils.tint(mToolbar.getNavigationIcon(), controlColor));

        updateToolbarMenu();
        updateLanguageToggle();
    }

    private void updateLanguageToggle() {
        // show or hide the title based on if we have visible language tabs
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(mLanguageTabs == null || mManifests.size() <= 1);
        }

        // update the styles for the language tabs
        if (mLanguageTabs != null) {
            mLanguageTabs.setVisibility(mManifests.size() > 1 ? View.VISIBLE : View.GONE);

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
            for (int i = 0; i < mLanguages.length; i++) {
                final TabLayout.Tab tab = mLanguageTabs.getTabAt(i);
                if (tab != null) {
                    // update tab visibility
                    TabLayoutUtils.setVisibility(tab, mManifests.get(i) != null ? View.VISIBLE : View.GONE);

                    // update tab background
                    Drawable bkg = AppCompatResources.getDrawable(mLanguageTabs.getContext(), R.drawable.bkg_tab_label);
                    if (bkg != null) {
                        bkg = DrawableCompat.wrap(bkg).mutate();
                        DrawableCompat.setTint(bkg, controlColor);
                    }
                    TabLayoutUtils.setBackground(tab, bkg);
                }
            }
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
        }
    }

    private void updateBackground() {
        final Manifest manifest = getActiveManifest();
        getWindow().getDecorView().setBackgroundColor(Manifest.getBackgroundColor(manifest));
        Manifest.bindBackgroundImage(manifest, mBackgroundImage);
    }

    private void setupPager() {
        if (mPager != null) {
            mPagerAdapter = new ManifestPagerAdapter();
            mPagerAdapter.setCallbacks(this);
            mPager.setAdapter(mPagerAdapter);
            getLifecycle().addObserver(mPagerAdapter);
            mPager.addOnPageChangeListener(new AnalyticsPageChangeListener());
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
            mPagerAdapter.setManifest(getActiveManifest());
        }
    }

    private void startLoaders() {
        final LoaderManager manager = getSupportLoaderManager();

        final ManifestLoaderCallbacks manifestCallbacks = new ManifestLoaderCallbacks();
        for (int i = 0; i < mLanguages.length; i++) {
            manager.initLoader(LOADER_MANIFEST_BASE + i, null, manifestCallbacks);
        }
    }

    private void shareCurrentTract() {
        final Manifest manifest = getActiveManifest();
        if (manifest != null) {
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_tract_subject, manifest.getTitle()));
            intent.putExtra(Intent.EXTRA_TEXT, URI_SHARE_BASE.buildUpon()
                    .appendPath(LocaleCompat.toLanguageTag(manifest.getLocale()))
                    .appendPath(manifest.getCode())
                    .appendPath("")
                    .build().toString());
            startActivity(Intent.createChooser(intent, getString(R.string.share_tract_title, manifest.getTitle())));
        }
    }

    class AnalyticsPageChangeListener extends ViewPager.SimpleOnPageChangeListener {
        @Override
        public void onPageSelected(final int position) {
            final Manifest manifest = getActiveManifest();
            if (manifest != null) {
                mAnalytics.trackScreen(manifest.getCode() + "-" + position);
            }
        }
    }

    class ManifestLoaderCallbacks extends SimpleLoaderCallbacks<Manifest> {
        @Nullable
        @Override
        public Loader<Manifest> onCreateLoader(final int id, @Nullable final Bundle args) {
            final int langId = id - LOADER_MANIFEST_BASE;
            if (mTool != null && langId >= 0 && langId < mLanguages.length) {
                return new TractManifestLoader(TractActivity.this, mTool, mLanguages[id - LOADER_MANIFEST_BASE]);
            }

            return null;
        }

        @Override
        public void onLoadFinished(@NonNull final Loader<Manifest> loader, @Nullable final Manifest manifest) {
            if (loader instanceof TractManifestLoader) {
                setManifest(((TractManifestLoader) loader).getLocale(), manifest);
            }
        }
    }
}
