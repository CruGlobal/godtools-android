package org.cru.godtools.databinding

import android.app.Activity
import android.app.Application
import android.view.LayoutInflater
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.testing.TestLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.cru.godtools.model.Language
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ListItemLanguageBindingTest {
    private lateinit var binding: ListItemLanguageBinding

    private val selected = MutableLiveData<Locale?>()

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        binding = ListItemLanguageBinding.inflate(LayoutInflater.from(activity), null, false)
        binding.lifecycleOwner = TestLifecycleOwner()
        binding.selected = selected
    }

    @Test
    fun `root View - isSelected - Language`() {
        binding.language = Language().apply { code = Locale.ENGLISH }
        selected.value = null
        binding.executePendingBindings()
        assertFalse(binding.root.isSelected)

        selected.value = Locale.ENGLISH
        binding.executePendingBindings()
        assertTrue(binding.root.isSelected)

        selected.value = null
        binding.executePendingBindings()
        assertFalse(binding.root.isSelected)

        selected.value = Locale.FRENCH
        binding.executePendingBindings()
        assertFalse(binding.root.isSelected)
    }

    @Test
    fun `root View - isSelected - None option`() {
        // the "None" option is represented by a null language
        binding.language = null
        selected.value = null
        binding.executePendingBindings()
        assertTrue(binding.root.isSelected)

        selected.value = Locale.ENGLISH
        binding.executePendingBindings()
        assertFalse(binding.root.isSelected)
    }

    @Test
    fun `root View - isSelected - Data Binding Locale Equality Bug`() {
        binding.language = Language().apply { code = Locale("en") }
        selected.value = Locale("en")
        binding.executePendingBindings()
        assertTrue(binding.root.isSelected)
    }
}
