package org.cru.godtools.base.tool.model.view

import android.content.Context
import org.cru.godtools.base.ui.util.getTypeface
import org.cru.godtools.xml.model.Manifest

fun Manifest.getTypeface(context: Context) = context.getTypeface(locale)
