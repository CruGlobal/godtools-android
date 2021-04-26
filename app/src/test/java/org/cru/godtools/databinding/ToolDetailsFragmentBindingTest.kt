package org.cru.godtools.databinding

import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.tips.Tip
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class ToolDetailsFragmentBindingTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var binding: ToolDetailsFragmentBinding

    @Before
    fun createBinding() {
        val activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()
        binding = ToolDetailsFragmentBinding.inflate(LayoutInflater.from(activity), null, false)
    }

    @Test
    fun verifyTrainingVisibleWhenToolHasTips() {
        binding.manifest = MutableLiveData(Manifest(tips = { listOf(Tip(it, "test1")) }))
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyTrainingHiddenWhenToolHasNoTips() {
        binding.manifest = MutableLiveData(Manifest())
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyTrainingHiddenWhenManifestIsntAvailable() {
        binding.manifest = MutableLiveData(null)
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.actionToolTraining.visibility)
    }
}
