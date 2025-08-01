@file:JvmName("ModelUtils")

package org.cru.godtools.base.ui.util

import android.content.Context
import android.content.res.Resources
import java.util.Locale
import org.ccci.gto.android.common.util.content.localizeIfPossible
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.ui.BuildConfig
import org.cru.godtools.ui.R
import timber.log.Timber

@JvmName("getTranslationName")
fun Translation?.getName(tool: Tool?, context: Context?) = this?.name ?: tool?.name ?: ""

@JvmName("getTranslationDescription")
fun Translation?.getDescription(tool: Tool?, context: Context?) = this?.description ?: tool?.description ?: ""

// region Tool Category
@JvmName("getToolCategory")
fun Tool?.getCategory(context: Context, locale: Locale? = null) = getToolCategoryName(this?.category, context, locale)
fun getToolCategoryName(category: String?, context: Context, locale: Locale? = null) =
    category?.let { c -> context.localizeIfPossible(locale).getToolCategoryStringRes(c) ?: c }.orEmpty()

private fun Context.getToolCategoryStringRes(category: String) = when (category.lowercase(Locale.ROOT)) {
    Tool.CATEGORY_GOSPEL -> getString(R.string.tool_category_gospel)
    Tool.CATEGORY_ARTICLES -> getString(R.string.tool_category_articles)
    Tool.CATEGORY_CONVERSATION_STARTERS -> getString(R.string.tool_category_conversation_starter)
    Tool.CATEGORY_GROWTH -> getString(R.string.tool_category_growth)
    Tool.CATEGORY_TRAINING -> getString(R.string.tool_category_training)
    else -> {
        val e = Resources.NotFoundException("tool_category_$category was not found")
        when {
            BuildConfig.DEBUG -> throw e
            else -> Timber.tag("ToolCategory").e(e, "Missing Tool Category string: $category")
        }
        null
    }
}
// endregion Tool Category
