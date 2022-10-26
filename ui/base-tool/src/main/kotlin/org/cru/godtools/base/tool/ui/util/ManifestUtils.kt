package org.cru.godtools.base.tool.ui.util

import android.content.Context
import org.cru.godtools.base.ui.util.getTypeface
import org.cru.godtools.shared.tool.parser.model.Manifest

fun Manifest.getTypeface(context: Context) = context.getTypeface(locale)
