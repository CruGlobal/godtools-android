package org.cru.godtools.tool.tips

import org.cru.godtools.tool.model.tips.Tip

interface ShowTipCallback {
    fun showTip(tip: Tip)
}
