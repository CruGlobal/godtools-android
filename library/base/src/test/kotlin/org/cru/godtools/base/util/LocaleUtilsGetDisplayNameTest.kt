package org.cru.godtools.base.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        }
    }

    @Test
    fun preferLanguageNameString() {
        assertEquals("Language Name fa", Locale.forLanguageTag("fa").getDisplayName(context, defaultName = "invalid"))
        assertEquals("Language Name fil", Locale.forLanguageTag("fil").getDisplayName(context, defaultName = "invalid"))
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
