package org.cru.godtools.base.ui.util

import android.content.Context
import org.cru.godtools.base.ui.R

@Deprecated("This can be removed once all translations have been updated to use the correct string format")
private const val SHARE_LINK = "{{share_link}}"

fun Context.getShareMessage(url: String) = getString(R.string.share_general_message, url)
    .replace(SHARE_LINK, url)
