package org.cru.godtools.ui.languages

import android.app.Application
import android.view.LayoutInflater
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verify
import org.cru.godtools.databinding.LanguageSettingsFragmentBinding
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.activity.MainActivity
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
    fun verifyEditPrimaryLanguageAction() {
        reset(callbacks)

        binding.primaryLanguageButton.performClick()
        verify(callbacks).editPrimaryLanguage()
        verify(callbacks, never()).editParallelLanguage()
    }
    // endregion Primary Language

    // region Parallel Language
    @Test
    fun verifyEditParallelLanguageAction() {
        reset(callbacks)

        binding.parallelLanguageButton.performClick()
        verify(callbacks, never()).editPrimaryLanguage()
        verify(callbacks).editParallelLanguage()
    }
    // endregion Parallel Language
}
