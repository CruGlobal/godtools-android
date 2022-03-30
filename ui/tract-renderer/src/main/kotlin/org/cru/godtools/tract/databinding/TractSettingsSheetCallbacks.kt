package org.cru.godtools.tract.databinding

import org.cru.godtools.tool.model.shareable.ShareableImage

interface TractSettingsSheetCallbacks {
    fun shareLink()
    fun shareScreen()
    fun shareShareable(shareable: ShareableImage?)
    fun toggleTrainingTips()
    fun swapLanguages()
}
