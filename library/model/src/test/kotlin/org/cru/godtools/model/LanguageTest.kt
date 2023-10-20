package org.cru.godtools.model

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.Test
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.converter.LocaleTypeConverter
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.model.Language.Companion.primaryCollator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class LanguageTest {
    // region jsonapi parsing
    private val jsonApiConverter by lazy {
        JsonApiConverter.Builder()
            .addClasses(Language::class.java)
            .addConverters(LocaleTypeConverter)
            .build()
    }

    @Test
    fun testJsonApiParsing() {
        val language = parseJson("language.json")

        assertEquals(1, language.id)
        assertEquals(Locale.ENGLISH, language.code)
        assertEquals("English", language.name)
    }

    private fun parseJson(file: String) = this::class.java.getResourceAsStream(file)!!.reader()
        .use { jsonApiConverter.fromJson(it.readText(), Language::class.java).dataSingle!! }
    // endregion jsonapi parsing

    // region getDisplayName()
    @Test
    fun `getDisplayName()`() {
        mockkStatic("org.cru.godtools.base.util.LocaleUtils") {
            every { any<Locale>().getDisplayName(any(), any(), any()) } returns "DisplayName"

            val context: Context = mockk()
            val inLocale: Locale = Locale.CANADA_FRENCH
            assertEquals("DisplayName", Language(Locale.ENGLISH) { name = "name" }.getDisplayName(context, inLocale))
            verifyAll {
                Locale.ENGLISH.getDisplayName(context, "name", inLocale)
            }
        }
    }
    // endregion getDisplayName()

    @Test
    fun `primaryCollator - Doesn't crash on null Locale`() {
        assertNotNull((null as Locale?).primaryCollator)
    }
}
