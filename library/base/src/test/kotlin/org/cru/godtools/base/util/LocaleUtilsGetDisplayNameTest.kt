package org.cru.godtools.base.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.cru.godtools.base.R
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

private const val LANGUAGE_NAME_TAGLISH = "Language Name Taglish"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LocaleUtilsGetDisplayNameTest {
    private lateinit var context: Context

    @BeforeTest
    fun setup() {
        context = mockk {
            every { resources } returns mockk {
                every { configuration } returns Configuration()
            }
            every { createConfigurationContext(any()) } returns this
            every { getString(R.string.language_name_fa) } returns "Language Name fa"
            every { getString(R.string.language_name_fil) } returns "Language Name fil"
            every { getString(R.string.language_name_sid) } returns "Language Name sid"
            every { getString(R.string.language_name_fil_x_taglish) } returns LANGUAGE_NAME_TAGLISH
        }
    }

    @Test
    fun preferLanguageNameString() {
        assertEquals("Language Name fa", Locale.forLanguageTag("fa").getDisplayName(context, defaultName = "invalid"))
        assertEquals("Language Name fil", Locale.forLanguageTag("fil").getDisplayName(context, defaultName = "invalid"))
        assertEquals("Language Name sid", Locale.forLanguageTag("sid").getDisplayName(context, defaultName = "invalid"))
        assertEquals("Language Name sid", Locale.forLanguageTag("sId").getDisplayName(context, defaultName = "invalid"))
        assertEquals(
            LANGUAGE_NAME_TAGLISH,
            Locale.forLanguageTag("fil-x-taglish").getDisplayName(context, defaultName = "invalid")
        )
    }

    @Test
    fun `Language Name String - Performance - Don't localize context when not necessary`() {
        assertEquals("anglais", Locale.ENGLISH.getDisplayName(context, inLocale = Locale.FRENCH))
        verify { context wasNot Called }
    }

    @Test
    fun preferLanguageNameStringInDifferentLocale() {
        assertEquals(
            "Language Name fa",
            Locale.forLanguageTag("fa").getDisplayName(context, defaultName = "invalid", inLocale = Locale.FRENCH)
        )
        verify(exactly = 1) { context.createConfigurationContext(match { it.locale == Locale.FRENCH }) }
    }

    @Test
    fun preferSystemDisplayName() {
        assertEquals("English", Locale.ENGLISH.getDisplayName(context = context, defaultName = "invalid"))
    }

    @Test
    fun preferDefaultNameParameter() {
        assertEquals("Parameter", Locale("x").getDisplayName(context = context, defaultName = "Parameter"))
    }

    @Test
    fun fallbackToLanguageCode() {
        assertEquals("x", Locale("x").getDisplayName(context = context))
    }
}
