package org.cru.godtools.base.tool.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.distinctUntilChanged
import java.util.Locale
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.model.Language
import org.cru.godtools.tool.model.Manifest

abstract class BaseSingleToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int,
    private val requireTool: Boolean,
    private val supportedType: Manifest.Type?
) : BaseToolActivity<B>(contentLayoutId) {
    override val activeManifestLiveData get() = dataModel.manifest

    protected open val dataModel: BaseSingleToolActivityDataModel by viewModels()
    protected val manifestDataModel: LatestPublishedManifestDataModel get() = dataModel

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.let { extras ->
            dataModel.toolCode.value = extras.getString(EXTRA_TOOL, dataModel.toolCode.value)
            dataModel.locale.value = extras.getLocale(EXTRA_LANGUAGE, dataModel.locale.value)
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish()
            return
        }

        startLoaders()
    }
    // endregion Lifecycle

    private fun hasTool() = dataModel.toolCode.value != null && dataModel.locale.value != null

    protected val tool: String
        get() = when {
            !requireTool -> throw UnsupportedOperationException(
                "You cannot get the tool code on a fragment that doesn't require a tool"
            )
            else -> checkNotNull(dataModel.toolCode.value) { "requireTool is true, but a tool wasn't specified" }
        }

    protected val locale: Locale
        get() = when {
            !requireTool -> throw UnsupportedOperationException(
                "You cannot get the locale on a fragment that doesn't require a tool"
            )
            else -> checkNotNull(dataModel.locale.value?.takeUnless { it == Language.INVALID_CODE }) {
                "requireTool is true, but a valid locale wasn't specified"
            }
        }

    override fun cacheTools() {
        val toolCode = dataModel.toolCode.value ?: return
        val locale = dataModel.locale.value ?: return
        downloadManager.downloadLatestPublishedTranslationAsync(toolCode, locale)
    }

    override val activeDownloadProgressLiveData get() = dataModel.downloadProgress
    override val activeToolStateLiveData by lazy {
        activeManifestLiveData.combineWith(dataModel.translation, isConnected) { m, t, isConnected ->
            ToolState.determineToolState(m, t, manifestType = supportedType, isConnected = isConnected)
        }.distinctUntilChanged()
    }

    private fun validStartState() = !requireTool || hasTool()

    private fun startLoaders() {
        dataModel.manifest.observe(this) { onUpdateActiveManifest() }
    }

    // region Up Navigation
    override fun buildParentIntentExtras(): Bundle {
        val extras = super.buildParentIntentExtras()
        extras.putString(EXTRA_TOOL, dataModel.toolCode.value)
        extras.putLocale(EXTRA_LANGUAGE, dataModel.locale.value)
        return extras
    }
    // endregion Up Navigation

    companion object {
        fun buildExtras(context: Context, toolCode: String?, language: Locale?) = buildExtras(context).apply {
            putString(EXTRA_TOOL, toolCode)
            putLocale(EXTRA_LANGUAGE, language)
        }
    }
}
