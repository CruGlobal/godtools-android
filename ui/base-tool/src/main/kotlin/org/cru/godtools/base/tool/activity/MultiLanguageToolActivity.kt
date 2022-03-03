package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.map
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayout
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.ccci.gto.android.common.androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.lifecycle.observeOnce
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.analytics.model.ToggleLanguageAnalyticsActionEvent
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.navBarColor
import org.cru.godtools.tool.model.navBarControlColor

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
        setupActiveTranslationManagement()
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
    protected open val dataModel: MultiLanguageToolActivityDataModel by viewModels()

    private fun setupDataModel() {
        dataModel.supportedType.value = supportedType
    }
    // endregion Data Model

    // region Intent Processing
    override fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        intent?.extras?.let { extras ->
            val locales = extras.getLocaleArray(EXTRA_LANGUAGES)?.filterNotNull().orEmpty()
            dataModel.primaryLocales.value = locales.take(1)
            dataModel.parallelLocales.value = locales.drop(1)
        }
    }

    override val isValidStartState
        get() = dataModel.toolCode.value != null &&
            (!dataModel.primaryLocales.value.isNullOrEmpty() || !dataModel.parallelLocales.value.isNullOrEmpty())
    // endregion Intent Processing

    // region UI
    override val activeDownloadProgressLiveData get() = dataModel.activeToolDownloadProgress

    private fun setupActionBarTitle() {
        dataModel.activeLocale.combineWith(dataModel.visibleLocales) { active, locales ->
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

    override fun cacheTools() {
        dataModel.toolCode.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.downloadLatestPublishedTranslationAsync(tool, it) }
        }
    }
    // endregion Tool sync

    // region Active Translation management
    override val activeManifestLiveData get() = dataModel.activeManifest
    override val activeToolLoadingStateLiveData get() = dataModel.activeLoadingState

    private fun setupActiveTranslationManagement() {
        dataModel.locales.map { it.firstOrNull() }.notNull().observeOnce(this) {
            if (dataModel.activeLocale.value == null) dataModel.activeLocale.value = it
        }

        dataModel.availableLocales.observe(this) {
            updateActiveLocaleToAvailableLocaleIfNecessary(availableLocales = it)
        }
        dataModel.activeLoadingState.observe(this) {
            updateActiveLocaleToAvailableLocaleIfNecessary(activeLoadingState = it)
        }
        dataModel.loadingState.observe(this) { updateActiveLocaleToAvailableLocaleIfNecessary(loadingState = it) }
    }

    private fun updateActiveLocaleToAvailableLocaleIfNecessary(
        activeLoadingState: LoadingState? = dataModel.activeLoadingState.value,
        availableLocales: List<Locale> = dataModel.availableLocales.value.orEmpty(),
        loadingState: Map<Locale, LoadingState> = dataModel.loadingState.value.orEmpty()
    ) {
        when (activeLoadingState) {
            // update the active language if the current active language is not found, invalid, or offline
            LoadingState.NOT_FOUND,
            LoadingState.INVALID_TYPE,
            LoadingState.OFFLINE -> availableLocales.firstOrNull {
                loadingState[it] != LoadingState.NOT_FOUND && loadingState[it] != LoadingState.INVALID_TYPE &&
                    loadingState[it] != LoadingState.OFFLINE
            }?.let { dataModel.activeLocale.value = it }
            else -> Unit
        }
    }
    // endregion Active Translation management
}
