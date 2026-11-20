package org.cru.godtools.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.model.Language.Companion.getSortedDisplayNames
import org.cru.godtools.model.Language.Companion.sortedByDisplayName

class LanguageTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addConverters(LocaleTypeConverter)
            .build()
    }

    @Test
    fun `jsonapi parsing - valid`() {
        val language = parseJson("language.json")

        assertTrue(language.isValid)
        assertEquals(1, language.apiId)
        assertEquals(true, language.isForcedName)
        assertEquals(Locale.ENGLISH, language.code)
        assertEquals("English", language.name)
    }

    @Test
    fun `jsonapi parsing - invalid - code missing`() {
        val language = parseJson("language_invalid_code_missing.json")
        assertFalse(language.isValid, "missing language code is invalid")
    }

    @Test
    fun `jsonapi parsing - invalid - code null`() {
        val language = parseJson("language_invalid_code_null.json")
        assertFalse(language.isValid, "null language code is invalid")
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Language::class.java).dataSingle!! }
    // endregion jsonapi parsing

    // region getDisplayName()
    @Test
    fun `getDisplayName()`() {
        mockkStatic("org.cru.godtools.base.util.LocaleKt") {
            every { any<Locale>().getDisplayName(any(), any(), any()) } returns "DisplayName"

            val context: Context = mockk()
            val inLocale: Locale = Locale.CANADA_FRENCH
            assertEquals("DisplayName", Language(Locale.ENGLISH, name = "name").getDisplayName(context, inLocale))
            verifyAll {
                Locale.ENGLISH.getDisplayName(context, "name", inLocale)
            }
        }
    }

    @Test
    fun `getDisplayName() - isForcedName=true`() {
        val inLocale: Locale = Locale.ENGLISH
        val language = Language(
            code = inLocale,
            name = "English - Tester",
            isForcedName = true
        )
        val context: Context = mockk()

        assertEquals("English - Tester", language.getDisplayName(context, inLocale))
    }
    // endregion getDisplayName()

    // region sortedByDisplayName()
    @Test
    fun `sortedByDisplayName()`() {
        val french = Language(Locale.FRENCH, name = "French", isForcedName = true)
        val english = Language(Locale.ENGLISH, name = "english", isForcedName = true)
        val german = Language(Locale.GERMAN, name = "German", isForcedName = true)

        assertEquals(
            listOf(english, french, german),
            listOf(french, german, english).sortedByDisplayName(mockk(), Locale.ENGLISH),
        )
    }

    @Test
    fun `sortedByDisplayName() - keeps languages with the same display name`() {
        val filipino = Language(Locale.forLanguageTag("fil"), name = "Filipino", isForcedName = true)
        val taglish = Language(Locale.forLanguageTag("fil-x-taglish"), name = "Filipino", isForcedName = true)
        val filipinoLower = Language(Locale.forLanguageTag("fil-PH"), name = "filipino", isForcedName = true)

        assertEquals(
            listOf(filipino, taglish, filipinoLower),
            listOf(filipino, taglish, filipinoLower).sortedByDisplayName(mockk(), Locale.ENGLISH),
        )
    }
    // endregion sortedByDisplayName()

    // region getSortedDisplayNames()
    @Test
    fun `getSortedDisplayNames()`() {
        val languages = listOf(
            Language(Locale.FRENCH, name = "French", isForcedName = true),
            Language(Locale.forLanguageTag("fil"), name = "Filipino", isForcedName = true),
            Language(Locale.forLanguageTag("fil-x-taglish"), name = "Filipino", isForcedName = true),
            Language(Locale.ENGLISH, name = "english", isForcedName = true),
        )

        assertEquals(listOf("english", "Filipino", "French"), languages.getSortedDisplayNames(mockk(), Locale.ENGLISH))
    }
    // endregion getSortedDisplayNames()
}
