package org.cru.godtools.base.tool.ui.controller.cache

import org.cru.godtools.xml.model.Base

fun interface VariationResolver {
    fun resolve(model: Base): Int?
}
