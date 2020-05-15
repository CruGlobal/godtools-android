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
import org.ccci.gto.android.common.util.os.putLocaleArray
import org.cru.godtools.base.Constants.EXTRA_TOOL
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.tract.R
import org.cru.godtools.tract.adapter.ManifestPagerAdapter
import org.cru.godtools.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.service.FollowupService
import org.cru.godtools.xml.model.Manifest
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
        item.itemId == android.R.id.home && InstantApps.isInstantApp(this) -> {
            // handle close button if this is an instant app
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
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
    @VisibleForTesting(otherwise = PROTECTED)
    fun Uri.extractPageFromDeepLink() = pathSegments.getOrNull(2)?.toIntOrNull()
    // endregion Intent Processing

    override val activeManifest get() = dataModel.activeManifest.value

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
    protected val pagerAdapter by lazy {
        ManifestPagerAdapter().also {
            it.setCallbacks(this)
            lifecycle.addObserver(it)
            dataModel.activeManifest.observe(this, it)
        }
    }
    // endregion Tool Pager
    // endregion UI

    override fun cacheTools() {
        dataModel.tool.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.cacheTranslation(tool, it) }
        }
    }

    // region Share Link Logic
    override fun hasShareLinkUri() = activeManifest != null
    // endregion Share Link Logic
}
