package org.cru.godtools.base.tool.ui.util

import android.content.Context
import androidx.annotation.StringRes
import java.util.Locale
import org.ccci.gto.android.common.base.Constants.INVALID_STRING_RES
import org.ccci.gto.android.common.util.content.getString
import org.cru.godtools.base.tool.R
import org.cru.godtools.tool.model.Input

// TODO: formatArgs doesn't provide good encapsulation.
//       The caller needs to know what formatArgs the strings being used internally require.
//       This might be a good usage for tool state once that concept is implemented
fun Input.Error?.getMessage(context: Context, locale: Locale?, vararg formatArgs: Any?) = when (this) {
    is Input.Error.InvalidEmail, is Input.Error.Required -> context.getString(locale, msgId, *formatArgs)
    null -> null
    else -> ""
}

@get:StringRes
private val Input.Error.msgId get() = when (this) {
    is Input.Error.Required -> R.string.tract_content_input_error_required
    is Input.Error.InvalidEmail -> R.string.tract_content_input_error_invalid_email
    else -> INVALID_STRING_RES
}
