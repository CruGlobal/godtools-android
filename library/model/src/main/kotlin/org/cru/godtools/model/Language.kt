package org.cru.godtools.model

import android.content.Context
import androidx.annotation.RestrictTo
import java.text.Collator
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiAttribute
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiId
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiIgnore
import org.ccci.gto.android.common.jsonapi.annotation.JsonApiType
import org.cru.godtools.base.appLanguage
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.model.Base.Companion.INVALID_ID

private const val JSON_CODE = "code"
private const val JSON_NAME = "name"

@JsonApiType(Language.JSONAPI_TYPE)
class Language {
    companion object {
        const val JSONAPI_TYPE = "language"

        val JSONAPI_FIELDS = arrayOf(JSON_CODE, JSON_NAME)

        val INVALID_CODE = Locale("x", "inv")

        fun displayNameComparator(context: Context, displayLocale: Locale = context.appLanguage): Comparator<Language> =
            compareBy(displayLocale.primaryCollator) { it.getDisplayName(context, displayLocale) }

        private fun Collection<Language>.toDisplayNameSortedMap(context: Context, displayLocale: Locale) =
            associateBy { it.getDisplayName(context, displayLocale) }.toSortedMap(displayLocale.primaryCollator)

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

        private val Locale.primaryCollator: Collator
            get() = Collator.getInstance(this).also { it.strength = Collator.PRIMARY }
    }

    @JsonApiId
    private var _id: Long? = INVALID_ID
    var id: Long
        get() = _id ?: INVALID_ID
        set(id) {
            _id = id
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

    @JvmOverloads
    fun getDisplayName(context: Context?, inLocale: Locale? = context?.appLanguage) =
        _code?.getDisplayName(context, name, inLocale) ?: name ?: ""

    // XXX: output the language id and code for debugging purposes
    override fun toString() = "Language{id=$id, code=$_code}"

    val isValid get() = _code != null && code != INVALID_CODE
}

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
@Suppress("ktlint:standard:function-naming")
fun Language(code: Locale = Locale.ENGLISH, isAdded: Boolean = false, config: Language.() -> Unit = {}) =
    Language().apply {
        id = Random.nextLong()
        this.code = code
        name = UUID.randomUUID().toString()
        this.isAdded = isAdded
        config()
    }
