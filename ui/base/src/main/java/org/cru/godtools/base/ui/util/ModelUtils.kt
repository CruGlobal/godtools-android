@file:JvmName("ModelUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import java.util.Locale
import org.ccci.gto.android.common.util.content.localize
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

// region Tool Category
@JvmName("getToolCategory")
fun Tool?.getCategory(context: Context, locale: Locale? = null) =
    this?.category?.let { c -> (locale?.let { context.localize(it) } ?: context).getToolCategoryStringRes(c) ?: c }
        .orEmpty()

private const val STRING_RES_CATEGORY_NAME_PREFIX = "tool_category_"
private fun Context.getToolCategoryStringRes(category: String) =
    when (val id = resources.getIdentifier("$STRING_RES_CATEGORY_NAME_PREFIX$category", "string", packageName)) {
        0 -> null
        else -> resources.getString(id)
    }
// endregion Tool Category
