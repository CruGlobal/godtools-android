package org.cru.godtools.model

import android.content.Context
import java.util.Locale
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.appLanguage
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.base.util.getPrimaryCollator

private const val JSON_CODE = "code"
private const val JSON_NAME = "name"

@JsonApiType(Language.JSONAPI_TYPE)
data class Language(
    @JsonApiAttribute(JSON_CODE)
    val code: Locale,
    @JsonApiAttribute(JSON_NAME)
    val name: String? = null,
    @JsonApiIgnore
    val isAdded: Boolean = false,
    @JsonApiId
    val apiId: Long? = null,
) {
    internal constructor() : this(INVALID_CODE)

    companion object {
        const val JSONAPI_TYPE = "language"

        val JSONAPI_FIELDS = arrayOf(JSON_CODE, JSON_NAME)

        val INVALID_CODE = Locale("x", "inv")

        fun displayNameComparator(context: Context, displayLocale: Locale = context.appLanguage): Comparator<Language> =
            compareBy(displayLocale.getPrimaryCollator()) { it.getDisplayName(context, displayLocale) }

        private fun Collection<Language>.toDisplayNameSortedMap(context: Context, displayLocale: Locale) =
            associateBy { it.getDisplayName(context, displayLocale) }.toSortedMap(displayLocale.getPrimaryCollator())

        fun Collection<Language>.sortedByDisplayName(context: Context, displayLocale: Locale = context.appLanguage) =
            toDisplayNameSortedMap(context, displayLocale).values.toList()

        fun Collection<Language>.getSortedDisplayNames(context: Context, displayLocale: Locale = context.appLanguage) =
            toDisplayNameSortedMap(context, displayLocale).keys.toList()

        fun Collection<Language>.filterByDisplayAndNativeName(
            query: String,
            context: Context,
            appLanguage: Locale,
        ): List<Language> {
            val terms = query.split(Regex("\\s+")).filter { it.isNotBlank() }
            return filter {
                val displayName by lazy { it.getDisplayName(context, appLanguage) }
                val nativeName by lazy { it.getDisplayName(context, it.code) }
                terms.all { displayName.contains(it, true) || nativeName.contains(it, true) }
            }
        }
    }

    @Suppress("SENSELESS_COMPARISON")
    val isValid get() = code != INVALID_CODE && code != null

    @JvmOverloads
    fun getDisplayName(context: Context?, inLocale: Locale? = context?.appLanguage) =
        code.takeIf { isValid }?.getDisplayName(context, name, inLocale) ?: name ?: ""
}
