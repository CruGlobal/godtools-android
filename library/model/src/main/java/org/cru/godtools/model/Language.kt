package org.cru.godtools.model

import android.content.Context
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.util.getDisplayName
import java.util.Locale

private const val JSON_API_TYPE_LANGUAGE = "language"

private const val JSON_CODE = "code"
private const val JSON_NAME = "name"

@JsonApiType(JSON_API_TYPE_LANGUAGE)
class Language : Base() {
    companion object {
        @JvmField
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
    @JsonApiIgnore
    var isAdded = false

    fun getDisplayName(context: Context?) = _code?.getDisplayName(context, name, null) ?: ""

    // XXX: output the language id and code for debugging purposes
    override fun toString() = "Language{id=$id, code=$_code}"
}
