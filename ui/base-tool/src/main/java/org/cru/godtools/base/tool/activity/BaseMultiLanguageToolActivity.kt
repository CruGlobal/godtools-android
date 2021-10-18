package org.cru.godtools.base.tool.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import org.ccci.gto.android.common.util.os.getLocaleArray
import org.cru.godtools.base.EXTRA_LANGUAGES
import org.cru.godtools.base.EXTRA_TOOL

abstract class BaseMultiLanguageToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int
) : BaseToolActivity<B>(contentLayoutId) {
    protected open val dataModel: BaseMultiLanguageToolActivityDataModel by viewModels()

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
}
