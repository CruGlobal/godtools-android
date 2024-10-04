package org.cru.godtools.base.tool.databinding

import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage

interface ToolSettingsSheetCallbacks {
    fun shareShareable(shareable: ShareableImage?)
    fun swapLanguages()
}
