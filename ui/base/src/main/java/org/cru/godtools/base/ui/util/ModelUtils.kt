@file:JvmName("ModelUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import org.cru.godtools.base.ui.util.LocaleTypefaceUtils.safeApplyTypefaceSpan
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

@JvmName("getTranslationName")
fun Translation?.getName(tool: Tool?, context: Context?) =
    this?.let { safeApplyTypefaceSpan(name, getTypeface(context)) } ?: tool?.name ?: ""

@JvmName("getTranslationDescription")
fun Translation?.getDescription(tool: Tool?, context: Context?) =
    this?.let { safeApplyTypefaceSpan(description, getTypeface(context)) } ?: tool?.description ?: ""

private fun Translation.getTypeface(context: Context?) = LocaleTypefaceUtils.getTypeface(context, languageCode)
