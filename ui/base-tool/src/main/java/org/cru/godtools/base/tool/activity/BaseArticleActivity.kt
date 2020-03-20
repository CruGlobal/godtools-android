package org.cru.godtools.base.tool.activity

import org.cru.godtools.xml.model.Manifest

abstract class BaseArticleActivity(requireTool: Boolean = true) : BaseSingleToolActivity(false, requireTool) {
    override fun isSupportedType(type: Manifest.Type) = type == Manifest.Type.ARTICLE
}
