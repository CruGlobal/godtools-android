package org.cru.godtools.tool.tips

import org.cru.godtools.shared.tool.parser.model.tips.Tip

interface ShowTipCallback {
    fun showTip(tip: Tip)
}
