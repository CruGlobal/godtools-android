package org.cru.godtools.model

import android.content.Context
import java.text.Collator
import java.util.Locale
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.util.getDisplayName

private const val JSON_API_TYPE_LANGUAGE = "language"

private const val JSON_CODE = "code"
private const val JSON_NAME = "name"

@JsonApiType(JSON_API_TYPE_LANGUAGE)
class Language : Base() {
    companion object {
        val INVALID_CODE = Locale("x", "inv")
    }

    @JsonApiAttribute(JSON_CODE)
    private var _code: Locale? = null
    var code: Locale
        get() = _code ?: INVALID_CODE
        set(code) {
            _code = code
        }

    @JsonApiAttribute(JSON_NAME)
    var name: String? = null

    val isValid get() = code != null && code != INVALID_CODE

    fun getDisplayName(context: Context?) = getDisplayName(context, null)
    fun getDisplayName(context: Context?, inLocale: Locale?) =
        _code?.getDisplayName(context, name, inLocale) ?: name ?: ""

    // XXX: output the language id and code for debugging purposes
    override fun toString() = "Language{id=$id, code=$_code}"
}

fun Collection<Language>.toDisplayNameSortedMap(context: Context?, displayLocale: Locale? = null) =
    associateBy { it.getDisplayName(context, displayLocale) }
        .toSortedMap(Collator.getInstance(displayLocale).apply { strength = Collator.PRIMARY })

fun Collection<Language>.sortedByDisplayName(context: Context?, displayLocale: Locale? = null): List<Language> =
    toDisplayNameSortedMap(context, displayLocale).values.toList()
