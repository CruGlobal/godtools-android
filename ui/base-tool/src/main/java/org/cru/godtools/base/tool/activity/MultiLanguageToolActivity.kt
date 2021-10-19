package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.google.android.material.tabs.TabLayout
import java.util.Locale
import org.ccci.gto.android.common.compat.view.ViewCompat
import org.ccci.gto.android.common.util.graphics.toHsvColor
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.analytics.model.ToggleLanguageAnalyticsActionEvent
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.tool.model.navBarColor
import org.cru.godtools.tool.model.navBarControlColor

abstract class MultiLanguageToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int
) : BaseToolActivity<B>(contentLayoutId) {
    protected open val dataModel: BaseMultiLanguageToolActivityDataModel by viewModels()
    protected val toolState: ToolStateHolder by viewModels()

    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        setupLanguageToggle()
    }
    // endregion Lifecycle

    // region Intent Processing
    override fun processIntent(intent: Intent?, savedInstanceState: Bundle?) {
        intent?.extras?.let { extras ->
            dataModel.toolCode.value = extras.getString(EXTRA_TOOL, dataModel.toolCode.value)
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
    // region Language Toggle
    protected open val languageToggle: TabLayout? = null
    private lateinit var languageToggleController: LanguageToggleController
    private val languageToggleListener by lazy {
        object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (languageToggleController.isUpdatingTabs) return
                val locale = tab.tag as? Locale ?: return
                eventBus.post(ToggleLanguageAnalyticsActionEvent(dataModel.toolCode.value, locale))
                dataModel.setActiveLocale(locale)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        }
    }

    private fun setupLanguageToggle() {
        val languageToggle = languageToggle ?: return

        ViewCompat.setClipToOutline(languageToggle, true)
        dataModel.activeManifest.observe(this) { manifest ->
            // determine colors for the language toggle
            val controlColor = manifest.navBarControlColor
            var selectedColor = manifest.navBarColor
            if (Color.alpha(selectedColor) < 255) {
                // XXX: the expected behavior is to support transparent text. But we currently don't support
                //      transparent text, so pick white or black based on the control color
                selectedColor = if (controlColor.toHsvColor().value > 0.6) Color.BLACK else Color.WHITE
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
    // endregion UI

    // region Tool sync
    override val isInitialSyncFinished get() = dataModel.isInitialSyncFinished

    override fun cacheTools() {
        dataModel.toolCode.value?.let { tool ->
            dataModel.locales.value?.forEach { downloadManager.downloadLatestPublishedTranslationAsync(tool, it) }
        }
    }
    // endregion Tool sync
}
