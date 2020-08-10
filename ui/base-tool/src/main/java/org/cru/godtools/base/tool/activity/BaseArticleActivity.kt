package org.cru.godtools.base.tool.activity

import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.databinding.ViewDataBinding
import org.cru.godtools.base.tool.R
import org.cru.godtools.xml.model.Manifest

abstract class BaseArticleActivity<B : ViewDataBinding>(@LayoutRes contentLayoutId: Int, requireTool: Boolean = true) :
    BaseSingleToolActivity<B>(contentLayoutId, requireTool, Manifest.Type.ARTICLE) {
    @get:StringRes
    override val shareLinkMessageRes get() = R.string.share_tool_message_article
}
