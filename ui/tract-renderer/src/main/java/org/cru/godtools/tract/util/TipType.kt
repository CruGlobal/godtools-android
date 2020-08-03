package org.cru.godtools.tract.util

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import org.cru.godtools.tract.R
import org.cru.godtools.xml.model.tips.Tip

fun Tip.Type?.getDrawable(context: Context) = ContextCompat.getDrawable(context, drawableRes)
@get:DrawableRes
val Tip.Type?.drawableRes get() = when (this) {
    Tip.Type.ASK -> R.drawable.ic_tips_ask
    Tip.Type.CONSIDER -> R.drawable.ic_tips_consider
    Tip.Type.PREPARE -> R.drawable.ic_tips_prepare
    Tip.Type.QUOTE -> R.drawable.ic_tips_quote
    Tip.Type.TIP, null -> R.drawable.ic_tips_tip
}

@get:DrawableRes
val Tip.Type?.doneDrawableRes get() = when (this) {
    Tip.Type.ASK -> R.drawable.ic_tips_ask_done
    Tip.Type.CONSIDER -> R.drawable.ic_tips_consider_done
    Tip.Type.PREPARE -> R.drawable.ic_tips_prepare_done
    Tip.Type.QUOTE -> R.drawable.ic_tips_quote_done
    Tip.Type.TIP, null -> R.drawable.ic_tips_tip_done
}
