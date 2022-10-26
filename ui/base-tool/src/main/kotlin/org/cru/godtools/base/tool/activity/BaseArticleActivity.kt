package org.cru.godtools.base.tool.activity

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.tool.R

abstract class BaseArticleActivity<B : ViewDataBinding> protected constructor(
    @LayoutRes contentLayoutId: Int,
    requireTool: Boolean
) : BaseSingleToolActivity<B>(contentLayoutId, requireTool, Manifest.Type.ARTICLE) {
    protected constructor(@LayoutRes contentLayoutId: Int) : this(contentLayoutId, true)

    @get:StringRes
    override val shareLinkMessageRes get() = R.string.share_tool_message_article
}
