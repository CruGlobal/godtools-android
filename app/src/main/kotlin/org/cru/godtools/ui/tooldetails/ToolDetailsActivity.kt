package org.cru.godtools.ui.tooldetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.fragment.app.commit
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.cru.godtools.R
import org.cru.godtools.activity.BasePlatformActivity
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.BaseToolRendererModule
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.databinding.ActivityGenericFragmentWithNavDrawerBinding
import org.cru.godtools.download.manager.GodToolsDownloadManager
import org.cru.godtools.shortcuts.GodToolsShortcutManager

fun Activity.startToolDetailsActivity(toolCode: String) = startActivity(
    Intent(this, ToolDetailsActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_TOOL, toolCode)
)

@AndroidEntryPoint
class ToolDetailsActivity : BasePlatformActivity<ActivityGenericFragmentWithNavDrawerBinding>() {
    private val viewModel: ToolDetailsViewModel by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // finish now if we don't have a valid start state
        if (!isValidStartState) {
            finish()
            return
        }

        downloadLatestTranslation()
        createFragmentIfNeeded()
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.fragment_tool_details, menu)
        menu.setupPinShortcutAction()
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        title = ""
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_pin_shortcut -> {
            viewModel.shortcut.value?.let { shortcutManager.pinShortcut(it) }
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    override fun inflateBinding() = ActivityGenericFragmentWithNavDrawerBinding.inflate(layoutInflater)

    private val isValidStartState get() = viewModel.toolCode.value != null

    @MainThread
    private fun createFragmentIfNeeded() {
        with(supportFragmentManager) {
            if (primaryNavigationFragment != null) return

            commit {
                val fragment = ToolDetailsFragment()
                replace(org.cru.godtools.ui.R.id.frame, fragment)
                setPrimaryNavigationFragment(fragment)
            }
        }
    }

    // region Pin Shortcut
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    private fun Menu.setupPinShortcutAction() {
        findItem(R.id.action_pin_shortcut)?.let { item ->
            viewModel.shortcut.observe(this@ToolDetailsActivity, item) { isVisible = it != null }
        }
    }
    // endregion Pin Shortcut

    // region Training Tips
    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager
    @Inject
    @Named(BaseToolRendererModule.IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnected: LiveData<Boolean>

    private fun downloadLatestTranslation() {
        observe(viewModel.toolCode.asLiveData(), settings.primaryLanguageLiveData, isConnected) { t, l, _ ->
            if (t != null) downloadManager.downloadLatestPublishedTranslationAsync(t, l)
        }
    }
    // endregion Training Tips
}
