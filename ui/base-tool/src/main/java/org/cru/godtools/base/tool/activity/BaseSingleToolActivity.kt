package org.cru.godtools.base.tool.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.base.Constants.INVALID_LAYOUT_RES
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.Constants
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.model.Language
import org.cru.godtools.xml.model.Manifest
import java.util.Locale

abstract class BaseSingleToolActivity(
    immersive: Boolean,
    @LayoutRes contentLayoutId: Int = INVALID_LAYOUT_RES,
    private val requireTool: Boolean = true
) : BaseToolActivity(immersive, contentLayoutId) {
    override val activeManifestLiveData get() = dataModel.manifest

    private val dataModel: BaseSingleToolActivityDataModel by viewModels()
    protected val manifestDataModel: LatestPublishedManifestDataModel get() = dataModel

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.extras?.let { extras ->
            dataModel.toolCode.value = extras.getString(Constants.EXTRA_TOOL, dataModel.toolCode.value)
            dataModel.locale.value = extras.getLocale(Constants.EXTRA_LANGUAGE, dataModel.locale.value)
        }

        // finish now if this activity is in an invalid state
        if (!validStartState()) {
            finish()
            return
        }

        startLoaders()
        dataModel.downloadProgress.observe(this) { onDownloadProgressUpdated(it) }
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
        downloadManager.cacheTranslation(toolCode, locale)
    }

    override val activeToolStateLiveData by lazy {
        activeManifestLiveData.combineWith(dataModel.translation) { manifest, translation ->
            when {
                manifest?.type?.let { isSupportedType(it) } == false -> ToolState.INVALID_TYPE
                manifest != null -> ToolState.LOADED
                translation == null -> ToolState.NOT_FOUND
                else -> ToolState.LOADING
            }
        }.distinctUntilChanged()
    }

    protected abstract fun isSupportedType(type: Manifest.Type): Boolean

    private fun validStartState() = !requireTool || hasTool()

    private fun startLoaders() {
        dataModel.manifest.observe(this) { onUpdateActiveManifest() }
    }

    // region Up Navigation
    override fun buildParentIntentExtras(): Bundle {
        val extras = super.buildParentIntentExtras()
        extras.putString(Constants.EXTRA_TOOL, dataModel.toolCode.value)
        extras.putLocale(Constants.EXTRA_LANGUAGE, dataModel.locale.value)
        return extras
    }
    // endregion Up Navigation

    companion object {
        fun buildExtras(context: Context, toolCode: String?, language: Locale?) = buildExtras(context).apply {
            putString(Constants.EXTRA_TOOL, toolCode)
            putLocale(Constants.EXTRA_LANGUAGE, language)
        }
    }
}
