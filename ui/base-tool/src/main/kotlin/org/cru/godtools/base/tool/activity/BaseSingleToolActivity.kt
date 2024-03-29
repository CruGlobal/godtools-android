package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.lifecycleScope
import java.util.Locale
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.model.Language
import org.cru.godtools.shared.tool.parser.model.Manifest

abstract class BaseSingleToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int,
    private val requireTool: Boolean,
    private val supportedType: Manifest.Type?
) : BaseToolActivity<B>(contentLayoutId) {
    override val viewModel: BaseSingleToolActivityDataModel by viewModels()
    protected open val dataModel get() = viewModel

    // region Intent processing
    override fun processIntent(intent: Intent, savedInstanceState: Bundle?) {
        intent.extras?.let { extras ->
            dataModel.toolCode.value = extras.getString(EXTRA_TOOL, dataModel.toolCode.value)
            dataModel.locale.value = extras.getLocale(EXTRA_LANGUAGE, dataModel.locale.value)
        }
    }

    override val isValidStartState get() = super.isValidStartState && (!requireTool || hasTool())
    private fun hasTool() = !dataModel.toolCode.value.isNullOrEmpty() && dataModel.locale.value != null
    // endregion Intent processing

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val tool: String
        get() = when {
            !requireTool -> throw UnsupportedOperationException(
                "You cannot get the tool code on a fragment that doesn't require a tool"
            )
            else -> checkNotNull(dataModel.toolCode.value) { "requireTool is true, but a tool wasn't specified" }
        }

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val locale: Locale
        get() = when {
            !requireTool -> throw UnsupportedOperationException(
                "You cannot get the locale on a fragment that doesn't require a tool"
            )
            else -> checkNotNull(dataModel.locale.value?.takeUnless { it == Language.INVALID_CODE }) {
                "requireTool is true, but a valid locale wasn't specified"
            }
        }

    override val toolsToDownload by lazy {
        dataModel.toolCode
            .map { listOfNotNull(it) }
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(), emptyList())
    }
    override val localesToDownload by lazy {
        dataModel.locale
            .map { listOfNotNull(it) }
            .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(), emptyList())
    }

    override val activeToolLoadingStateLiveData by lazy {
        viewModel.manifest.asLiveData().combineWith(dataModel.translation, isConnected) { m, t, isConnected ->
            LoadingState.determineToolState(m, t, manifestType = supportedType, isConnected = isConnected)
        }.distinctUntilChanged()
    }

    // region Up Navigation
    override fun buildParentIntentExtras(): Bundle {
        val extras = super.buildParentIntentExtras()
        extras.putString(EXTRA_TOOL, dataModel.toolCode.value)
        extras.putLocale(EXTRA_LANGUAGE, dataModel.locale.value)
        return extras
    }
    // endregion Up Navigation
}
