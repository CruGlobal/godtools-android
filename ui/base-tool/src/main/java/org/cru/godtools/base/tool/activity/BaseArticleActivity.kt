package org.cru.godtools.base.tool.activity

import org.cru.godtools.base.tool.R
import org.cru.godtools.base.tool.databinding.ToolGenericFragmentActivityBinding
import org.cru.godtools.xml.model.Manifest

abstract class BaseArticleActivity(requireTool: Boolean = true) :
    BaseSingleToolActivity<ToolGenericFragmentActivityBinding>(
        false,
        contentLayoutId = R.layout.tool_generic_fragment_activity,
        requireTool = requireTool
    ) {
    override fun isSupportedType(type: Manifest.Type) = type == Manifest.Type.ARTICLE
}
