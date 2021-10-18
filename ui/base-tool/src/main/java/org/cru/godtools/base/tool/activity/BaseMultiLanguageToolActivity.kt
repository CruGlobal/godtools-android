package org.cru.godtools.base.tool.activity

import androidx.activity.viewModels
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding

abstract class BaseMultiLanguageToolActivity<B : ViewDataBinding>(
    @LayoutRes contentLayoutId: Int
) : BaseToolActivity<B>(contentLayoutId) {
    protected open val dataModel: BaseMultiLanguageToolActivityDataModel by viewModels()
}
