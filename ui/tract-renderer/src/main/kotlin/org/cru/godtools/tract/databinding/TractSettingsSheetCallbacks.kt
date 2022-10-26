package org.cru.godtools.tract.databinding

import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage

interface TractSettingsSheetCallbacks {
    fun shareLink()
    fun shareScreen()
    fun shareShareable(shareable: ShareableImage?)
    fun toggleTrainingTips()
    fun swapLanguages()
}
