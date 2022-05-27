package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import org.cru.godtools.model.Language
import org.cru.godtools.ui.languages.LanguageSettingsActivity
import org.cru.godtools.ui.languages.LanguageSettingsFragmentBindingCallbacks
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LanguageSettingsFragmentBindingTest {
    private lateinit var binding: LanguageSettingsFragmentBinding
    private val callbacks = mockk<LanguageSettingsFragmentBindingCallbacks>(relaxUnitFun = true)

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(LanguageSettingsActivity::class.java)

        binding = LanguageSettingsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
        binding.lifecycleOwner = activityController.get()
        binding.callbacks = callbacks
        binding.executePendingBindings()
    }

    // region Primary Language
    @Test
    fun verifyPrimaryLanguageLabel() {
        val language = mockk<Language> { every { getDisplayName(any()) } returns "Language Object" }
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
        binding.primaryLanguageButton.performClick()
        verifyAll { callbacks.editPrimaryLanguage() }
    }
    // endregion Primary Language

    // region Parallel Language
    @Test
    fun verifyParallelLanguageLabel() {
        val language = mockk<Language> { every { getDisplayName(any()) } returns "Language Object" }
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
        binding.parallelLanguageButton.performClick()
        verifyAll { callbacks.editParallelLanguage() }
    }
    // endregion Parallel Language
}
