package org.cru.godtools.base.tool.activity

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import org.cru.godtools.xml.model.Manifest

abstract class BaseArticleActivity<B : ViewDataBinding>(@LayoutRes contentLayoutId: Int, requireTool: Boolean = true) :
    BaseSingleToolActivity<B>(false, contentLayoutId, requireTool) {
    override fun isSupportedType(type: Manifest.Type) = type == Manifest.Type.ARTICLE
}
