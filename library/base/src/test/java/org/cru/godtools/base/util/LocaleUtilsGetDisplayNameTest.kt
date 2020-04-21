package org.cru.godtools.base.util

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.stubbing.OngoingStubbing
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LocaleUtilsGetDisplayNameTest {
    lateinit var context: Context

    @Before
    fun setup() {
        context = mock(defaultAnswer = RETURNS_DEEP_STUBS)
        whenever(context.packageName).thenReturn(null)
        whenever(context.resources.configuration).thenReturn(Configuration())
        whenever(context.createConfigurationContext(any())).thenReturn(context)
    }

    @Test
    fun preferLanguageNameString() {
        wheneverGetLanguageNameStringRes(Locale.ENGLISH).thenReturn("Language Name String")

        assertEquals("Language Name String", Locale.ENGLISH.getDisplayName(context, defaultName = "invalid"))
        verify(context, never()).createConfigurationContext(any())
    }

    @Test
    fun preferLanguageNameStringInDifferentLocale() {
        wheneverGetLanguageNameStringRes(Locale.ENGLISH).thenReturn("Language Name String")

        assertEquals(
            "Language Name String",
            Locale.ENGLISH.getDisplayName(context, defaultName = "invalid", inLocale = Locale.FRENCH)
        )
        verify(context).createConfigurationContext(argThat { locale == Locale.FRENCH })
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

    private fun wheneverGetLanguageNameStringRes(locale: Locale): OngoingStubbing<String?> {
        whenever(
            context.resources.getIdentifier(
                "$STRING_RES_LANGUAGE_NAME_PREFIX${locale.toString().toLowerCase(Locale.ENGLISH)}", "string", null
            )
        ).thenReturn(1)
        return whenever(context.resources.getString(1))
    }
}
