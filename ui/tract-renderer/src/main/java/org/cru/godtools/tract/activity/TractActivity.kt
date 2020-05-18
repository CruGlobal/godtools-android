// ktlint-disable filename
package org.cru.godtools.tract.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import androidx.annotation.VisibleForTesting.PROTECTED
import androidx.lifecycle.observe
import com.google.android.instantapps.InstantApps
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.ccci.gto.android.common.util.LocaleUtils
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.base.Constants.EXTRA_TOOL
import org.cru.godtools.base.Constants.URI_SHARE_BASE
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.model.Translation
import org.cru.godtools.tract.Constants.PARAM_PARALLEL_LANGUAGE
import org.cru.godtools.tract.Constants.PARAM_PRIMARY_LANGUAGE
import org.cru.godtools.tract.Constants.PARAM_USE_DEVICE_LANGUAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.analytics.model.TractPageAnalyticsScreenEvent
import org.cru.godtools.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.service.FollowupService
import org.cru.godtools.xml.model.Card
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Modal
import org.cru.godtools.xml.model.Page
import org.jetbrains.annotations.Contract
import java.util.Locale
import javax.inject.Inject

fun Activity.startTractActivity(toolCode: String, vararg languages: Locale?) =
    startActivity(createTractActivityIntent(toolCode, *languages))

fun Context.createTractActivityIntent(toolCode: String, vararg languages: Locale?) =
    Intent(this, TractActivity::class.java)
        .putExtras(Bundle().populateTractActivityExtras(toolCode, *languages))

private fun Bundle.populateTractActivityExtras(toolCode: String, vararg languages: Locale?) = apply {
    putString(EXTRA_TOOL, toolCode)
    // XXX: we use singleString mode to support using this intent for legacy shortcuts
    putLocaleArray(TractActivity.EXTRA_LANGUAGES, languages.filterNotNull().toTypedArray(), true)
}

abstract class KotlinTractActivity : BaseToolActivity(true), ManifestPagerAdapter.Callbacks {
    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    protected val dataModel: TractActivityDataModel by viewModels()

    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        setupBackground()
        startDownloadProgressListener()
        setupPager()
    }

    @CallSuper
    override fun onSetupActionBar() {
        super.onSetupActionBar()
        if (InstantApps.isInstantApp(this)) toolbar?.setNavigationIcon(R.drawable.ic_close)
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.activity_tract, menu)

        // make the install menu item visible if this is an Instant App
        menu.findItem(R.id.action_install)?.isVisible = InstantApps.isInstantApp(this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when {
        item.itemId == R.id.action_install -> {
            InstantApps.showInstallPrompt(this, -1, "instantapp")
            true
        }
        // handle close button if this is an instant app
        item.itemId == android.R.id.home && InstantApps.isInstantApp(this) -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onUpdateActiveCard(page: Page, card: Card?) {
        trackTractPage(page, card)
    }
    // endregion Lifecycle

    // region Intent Processing
    @Contract("null -> false")
    @VisibleForTesting(otherwise = PROTECTED)
    fun isDeepLinkValid(data: Uri?) = data != null &&
        ("http".equals(data.scheme, true) || "https".equals(data.scheme, true)) &&
        (getString(R.string.tract_deeplink_host_1).equals(data.host, true) ||
            getString(R.string.tract_deeplink_host_2).equals(data.host, true)) &&
        data.pathSegments.size >= 2

    @VisibleForTesting(otherwise = PROTECTED)
    fun Uri.extractToolFromDeepLink() = pathSegments.getOrNull(1)

    @OptIn(ExperimentalStdlibApi::class)
    protected fun Uri.extractLanguagesFromDeepLink(): Pair<List<Locale>, List<Locale>> {
        val primary = LocaleUtils.getFallbacks(*buildList<Locale> {
            if (!getQueryParameter(PARAM_USE_DEVICE_LANGUAGE).isNullOrEmpty()) add(Locale.getDefault())
            addAll(extractLanguagesFromDeepLinkParam(PARAM_PRIMARY_LANGUAGE))
            add(LocaleCompat.forLanguageTag(pathSegments[0]))
        }.toTypedArray())

        val parallel = LocaleUtils.getFallbacks(*extractLanguagesFromDeepLinkParam(PARAM_PARALLEL_LANGUAGE).toTypedArray())
        return Pair(primary.toList(), parallel.toList())
    }

    private fun Uri.extractLanguagesFromDeepLinkParam(param: String) = getQueryParameters(param)
        .flatMap { it.split(",") }
        .map { it.trim() }.filterNot { it.isEmpty() }
        .map { LocaleCompat.forLanguageTag(it) }
        .toList()

    @VisibleForTesting(otherwise = PROTECTED)
    fun Uri.extractPageFromDeepLink() = pathSegments.getOrNull(2)?.toIntOrNull()
    // endregion Intent Processing

    // region UI
    protected lateinit var binding: TractActivityBinding

    private fun setupBackground() {
        dataModel.activeManifest.observe(this) {
            window.decorView.setBackgroundColor(Manifest.getBackgroundColor(it))
            ManifestViewUtils.bindBackgroundImage(it, binding.mainContent.backgroundImage)
        }
    }

    private fun startDownloadProgressListener() {
        dataModel.downloadProgress.observe(this) { onDownloadProgressUpdated(it) }
    }

    // region Tool Pager
    protected val pager get() = binding.mainContent.pages
    protected val pagerAdapter by lazy {
        ManifestPagerAdapter().also {
            it.setCallbacks(this)
            lifecycle.addObserver(it)
            dataModel.activeManifest.observe(this, it)
        }
    }
    protected var initialPage = 0

    private fun setupPager() {
        pager.adapter = pagerAdapter

        if (initialPage >= 0) dataModel.activeManifest.notNull().observeOnce(this) {
            if (initialPage < 0) return@observeOnce

            // HACK: set the manifest in the pager adapter to ensure setCurrentItem works.
            //       This is normally handled by the pager adapter observer.
            pagerAdapter.setManifest(it)
            pager.setCurrentItem(initialPage, false)
            initialPage = -1
        }
    }

    // region ManifestPagerAdapter.Callbacks
    override fun goToPage(position: Int) {
        pager.currentItem = position
    }

    override fun showModal(modal: Modal) = startModalActivity(modal)
    // endregion ManifestPagerAdapter.Callbacks
    // endregion Tool Pager
    // endregion UI

    override fun cacheTools() {
        dataModel.tool.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.cacheTranslation(tool, it) }
        }
    }

    private fun trackTractPage(page: Page, card: Card?) = eventBus.post(
        TractPageAnalyticsScreenEvent(page.manifest.code, page.manifest.locale, page.position, card?.position)
    )

    // region Active Translation management
    override val activeManifest get() = dataModel.activeManifest.value

    override fun determineActiveToolState() = dataModel.activeState.value ?: STATE_LOADING
    // endregion Active Translation management

    // region Share Link Logic
    override fun hasShareLinkUri() = activeManifest != null
    override val shareLinkUri
        get() = activeManifest?.let {
            URI_SHARE_BASE.buildUpon()
                .appendEncodedPath(LocaleCompat.toLanguageTag(it.locale).toLowerCase(Locale.ENGLISH))
                .appendPath(it.code)
                .apply { if (pager.currentItem > 0) appendPath(pager.currentItem.toString()) }
                .appendQueryParameter("icid", "gtshare")
                .build().toString()
        }
    // endregion Share Link Logic

    companion object {
        internal fun determineState(manifest: Manifest?, translation: Translation?, isSyncRunning: Boolean?) = when {
            manifest != null && manifest.type != Manifest.Type.TRACT -> STATE_INVALID_TYPE
            manifest != null -> STATE_LOADED
            translation == null && isSyncRunning == false -> STATE_NOT_FOUND
            else -> STATE_LOADING
        }
    }
}
