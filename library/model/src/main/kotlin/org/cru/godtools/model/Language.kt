package org.cru.godtools.model

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import java.text.Collator
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.util.getDisplayName

private const val JSON_CODE = "code"
private const val JSON_NAME = "name"

@JsonApiType(Language.JSONAPI_TYPE)
class Language : Base() {
    companion object {
        const val JSONAPI_TYPE = "language"

        val JSONAPI_FIELDS = arrayOf(JSON_CODE, JSON_NAME)

        val INVALID_CODE = Locale("x", "inv")

        fun COMPARATOR_DISPLAY_NAME(context: Context? = null, displayLocale: Locale? = null): Comparator<Language> =
            compareBy(displayLocale.primaryCollator) { it.getDisplayName(context, displayLocale) }

        fun Collection<Language>.toDisplayNameSortedMap(context: Context?, displayLocale: Locale? = null) =
            associateBy { it.getDisplayName(context, displayLocale) }.toSortedMap(displayLocale.primaryCollator)

        fun Collection<Language>.sortedByDisplayName(context: Context?, displayLocale: Locale? = null): List<Language> =
            toDisplayNameSortedMap(context, displayLocale).values.toList()

        fun Collection<Language>.getSortedDisplayNames(context: Context?, displayLocale: Locale? = null) =
            toDisplayNameSortedMap(context, displayLocale).keys.toList()

        @VisibleForTesting
        internal val Locale?.primaryCollator: Collator
            get() = Collator.getInstance(this ?: Locale.getDefault()).also { it.strength = Collator.PRIMARY }
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
    var isAdded: Boolean = false

    fun getDisplayName(context: Context?) = getDisplayName(context, null)
    fun getDisplayName(context: Context?, inLocale: Locale?) =
        _code?.getDisplayName(context, name, inLocale) ?: name ?: ""

    // XXX: output the language id and code for debugging purposes
    override fun toString() = "Language{id=$id, code=$_code}"

    val isValid get() = _code != null && code != INVALID_CODE
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun Language(
    code: Locale = Locale.ENGLISH,
    isAdded: Boolean = false,
    config: Language.() -> Unit = {},
) = Language().apply {
    id = Random.nextLong()
    this.code = code
    name = UUID.randomUUID().toString()
    this.isAdded = isAdded
    config()
}
