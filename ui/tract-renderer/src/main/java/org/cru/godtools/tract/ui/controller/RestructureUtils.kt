package org.cru.godtools.tract.ui.controller

import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.xml.model.Base

// TODO: move this back into BaseController and make modelClass private again
internal fun <T : Base> BaseController<T>.releaseTo(cache: UiControllerCache) = cache.release(modelClass, this)
