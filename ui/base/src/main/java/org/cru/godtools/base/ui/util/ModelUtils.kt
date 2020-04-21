@file:JvmName("ModelUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation

@JvmName("getTranslationName")
fun Translation?.getName(tool: Tool?, context: Context?) =
    this?.let { name?.applyTypefaceSpan(getTypeface(context)) } ?: tool?.name ?: ""

@JvmName("getTranslationDescription")
fun Translation?.getDescription(tool: Tool?, context: Context?) =
    this?.let { description?.applyTypefaceSpan(getTypeface(context)) } ?: tool?.description ?: ""

@JvmName("getTranslationTagline")
fun Translation?.getTagline(tool: Tool?, context: Context?) =
    this?.let { (tagline ?: description)?.applyTypefaceSpan(getTypeface(context)) } ?: tool?.description ?: ""

private fun Translation.getTypeface(context: Context?) = context?.getTypeface(languageCode)
