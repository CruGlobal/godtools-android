package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.combine
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.tool.analytics.model.ToggleLanguageAnalyticsActionEvent
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.navBarColor
import org.cru.godtools.shared.tool.parser.model.navBarControlColor
import org.cru.godtools.tool.R

abstract class MultiLanguageToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int,
    private val supportedType: Manifest.Type
) : BaseToolActivity<B>(contentLayoutId) {
    protected val toolState: ToolStateHolder by viewModels()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isFinishing) return

        setupDataModel()
    }

    override fun onContentChanged() {
        super.onContentChanged()
        setupLanguageToggle()
    }

    override fun onSetupActionBar() {
        super.onSetupActionBar()
        setupActionBarTitle()
    }

    override fun onCreateOptionsMenu(menu: Menu) = super.onCreateOptionsMenu(menu).also {
        menuInflater.inflate(R.menu.activity_tool_multilanguage, menu)
        menu.setupTrainingTipsMenuItem()
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_tips -> {
            dataModel.showTips.value = !dataModel.showTips.value!!
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
    // endregion Lifecycle

    // region Data Model
    override val viewModel: MultiLanguageToolActivityDataModel by viewModels()
    protected open val dataModel get() = viewModel

    private fun setupDataModel() {
        dataModel.supportedType.value = supportedType
    }
    // endregion Data Model

    // region Intent Processing
    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        if (dataModel.locales.value.isNullOrEmpty()) {
            val extras = intent.extras ?: return
            val locales = extras.getLocaleArray(EXTRA_LANGUAGES)?.filterNotNull().orEmpty()
            dataModel.primaryLocales.value = locales.take(1)
            dataModel.parallelLocales.value = locales.drop(1)
        }
    }

    override val isValidStartState
        get() = !dataModel.toolCode.value.isNullOrEmpty() && !dataModel.locales.value.isNullOrEmpty()
    // endregion Intent Processing

    // region UI
    override val activeDownloadProgressLiveData get() = dataModel.activeToolDownloadProgress

    private fun setupActionBarTitle() {
        combine(dataModel.visibleLocales, dataModel.activeLocale) { locales, active ->
            locales.isEmpty() || (locales.size < 2 && locales.contains(active))
        }.observe(this) { supportActionBar?.setDisplayShowTitleEnabled(it) }
    }

    // region Language Toggle
    protected open val languageToggle: TabLayout? = null
    private lateinit var languageToggleController: LanguageToggleController
    private val languageToggleListener by lazy {
        object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (languageToggleController.isUpdatingTabs) return
                val locale = tab.tag as? Locale ?: return
                eventBus.post(ToggleLanguageAnalyticsActionEvent(dataModel.toolCode.value, locale))
                dataModel.activeLocale.value = locale
            }

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        }
    }

    private fun setupLanguageToggle() {
        val languageToggle = languageToggle ?: return

        languageToggle.clipToOutline = true
        dataModel.activeManifest.observe(this) { manifest ->
            // determine colors for the language toggle
            val controlColor = manifest.navBarControlColor
            var selectedColor = manifest.navBarColor
            if (Color.alpha(selectedColor) < 255) {
                // XXX: the expected behavior is to support transparent text. But we currently don't support
                //      transparent text, so pick white or black based on the control color
                selectedColor = if (MaterialColors.isColorLight(controlColor)) Color.BLACK else Color.WHITE
            }

            // update colors for tab text
            languageToggle.setTabTextColors(controlColor, selectedColor)
        }

        languageToggleController = LanguageToggleController(languageToggle).also { controller ->
            dataModel.activeLocale.observe(this) { controller.activeLocale = it }
            dataModel.activeManifest.observe(this) { controller.activeManifest = it }
            dataModel.visibleLocales.observe(this) { controller.locales = it }
            dataModel.languages.observe(this) { controller.languages = it }
        }

        languageToggle.addOnTabSelectedListener(languageToggleListener)
    }
    // endregion Language Toggle

    // region Training Tips
    private fun Menu.setupTrainingTipsMenuItem() {
        findItem(R.id.action_tips)?.let { item ->
            dataModel.hasTips.observe(this@MultiLanguageToolActivity, item) { isVisible = it }
            dataModel.showTips.observe(this@MultiLanguageToolActivity, item) { isChecked = it }
        }
    }
    // endregion Training Tips
    // endregion UI

    // region Tool sync
    override val isInitialSyncFinished get() = dataModel.isInitialSyncFinished
    override val toolsToDownload by lazy {
        dataModel.toolCode.map { listOfNotNull(it) }
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(), emptyList())
    }
    override val localesToDownload by lazy {
        dataModel.locales.asFlow()
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(), emptyList())
    }
    // endregion Tool sync

    // region Active Translation management
    override val activeToolLoadingStateLiveData get() = dataModel.activeLoadingState
    // endregion Active Translation management
}
