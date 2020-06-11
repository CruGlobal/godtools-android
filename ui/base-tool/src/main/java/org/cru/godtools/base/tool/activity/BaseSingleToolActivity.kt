package org.cru.godtools.base.tool.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.androidx.lifecycle.net.isConnectedLiveData
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.Constants
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.IS_CONNECTED_LIVE_DATA
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.model.Language
import org.cru.godtools.xml.model.Manifest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Named

abstract class BaseSingleToolActivity<B : ViewDataBinding>(
    immersive: Boolean,
    @LayoutRes contentLayoutId: Int,
    private val requireTool: Boolean = true
) : BaseToolActivity<B>(immersive, contentLayoutId) {
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

    @Inject
    @Named(IS_CONNECTED_LIVE_DATA)
    internal lateinit var isConnectedLiveData: LiveData<Boolean>

    override val activeDownloadProgressLiveData get() = dataModel.downloadProgress
    override val activeToolStateLiveData by lazy {
        activeManifestLiveData.combineWith(dataModel.translation, isConnectedLiveData) { m, t, isConnected ->
            ToolState.determineToolState(m, t, manifestType = supportedType, isConnected = isConnected)
        }.distinctUntilChanged()
    }

    protected open val supportedType: Manifest.Type? get() = null

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
