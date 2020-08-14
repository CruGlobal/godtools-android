package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.BuildConfig
import org.cru.godtools.ui.tooldetails.ToolDetailsActivity
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.tips.Tip
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeFalse
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsFragmentBindingTest {
    private lateinit var binding: ToolDetailsFragmentBinding

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(ToolDetailsActivity::class.java)
        binding = ToolDetailsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
    }

    @Test
    fun verifyTrainingHiddenForReleaseBuilds() {
        assumeFalse("This test only applies to release builds", BuildConfig.DEBUG)

        binding.manifest = MutableLiveData(Manifest(tips = { listOf(Tip(it, "test1")) }))
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.actionToolTraining.visibility)
        binding.manifest = MutableLiveData(Manifest())
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.actionToolTraining.visibility)
        binding.manifest = MutableLiveData(null)
        binding.executePendingBindings()
        assertEquals(View.GONE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyTrainingVisibleWhenToolHasTips() {
        assumeTrue("Training is currently disabled for non-debug builds", BuildConfig.DEBUG)

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
