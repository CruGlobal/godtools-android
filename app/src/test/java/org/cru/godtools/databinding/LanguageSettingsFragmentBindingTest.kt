package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.ui.languages.LanguageSettingsFragmentBindingCallbacks
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LanguageSettingsFragmentBindingTest {
    private lateinit var binding: LanguageSettingsFragmentBinding
    private lateinit var callbacks: LanguageSettingsFragmentBindingCallbacks

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(MainActivity::class.java)
        callbacks = mock()

        binding = LanguageSettingsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.lifecycleOwner = activityController.get()
        binding.callbacks = callbacks
        binding.executePendingBindings()
    }

    // region Primary Language
    @Test
    fun verifyPrimaryLanguageLabel() {
        val language = mock<Language> { on { getDisplayName(any()) } doReturn "Language Object" }
        binding.primaryLocale = MutableLiveData(Locale.ENGLISH)
        binding.primaryLanguage = MutableLiveData(language)
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals("Language Object", binding.primaryLanguageButton.text)
    }

    @Test
    fun verifyPrimaryLanguageLabelLocaleOnly() {
        binding.primaryLocale = MutableLiveData(Locale.ENGLISH)
        binding.primaryLanguage = MutableLiveData(null)
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals("English", binding.primaryLanguageButton.text)
    }

    @Test
    fun verifyEditPrimaryLanguageAction() {
        reset(callbacks)

        binding.primaryLanguageButton.performClick()
        verify(callbacks).editPrimaryLanguage()
        verify(callbacks, never()).editParallelLanguage()
    }
    // endregion Primary Language

    // region Parallel Language
    @Test
    fun verifyParallelLanguageLabel() {
        val language = mock<Language> { on { getDisplayName(any()) } doReturn "Language Object" }
        binding.parallelLocale = MutableLiveData(Locale.ENGLISH)
        binding.parallelLanguage = MutableLiveData(language)
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals("Language Object", binding.parallelLanguageButton.text)
    }

    @Test
    fun verifyParallelLanguageLabelLocaleOnly() {
        binding.parallelLocale = MutableLiveData(Locale.ENGLISH)
        binding.parallelLanguage = MutableLiveData(null)
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals("English", binding.parallelLanguageButton.text)
    }

    @Test
    fun verifyParallelLanguageLabelNoParallelLanguage() {
        binding.parallelLocale = MutableLiveData(null)
        binding.parallelLanguage = MutableLiveData(null)
        binding.invalidateAll()
        binding.executePendingBindings()

        assertEquals("Select a Parallel Language", binding.parallelLanguageButton.text)
    }

    @Test
    fun verifyEditParallelLanguageAction() {
        reset(callbacks)

        binding.parallelLanguageButton.performClick()
        verify(callbacks, never()).editPrimaryLanguage()
        verify(callbacks).editParallelLanguage()
    }
    // endregion Parallel Language
}
