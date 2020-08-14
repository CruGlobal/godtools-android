package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.ui.tooldetails.ToolDetailsActivity
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.tips.Tip
import org.junit.Assert.assertEquals
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
    fun verifyWithTips() {
        val manifest = Manifest(tips = { listOf(Tip(it, "test1")) })
        binding.manifest = MutableLiveData<Manifest>(manifest)
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyNoTips() {
        val manifest = Manifest()
        binding.manifest = MutableLiveData<Manifest>(manifest)
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyEmptyManifest() {
        binding.manifest = MutableLiveData<Manifest>()
        binding.executePendingBindings()

        assertEquals(View.GONE, binding.actionToolTraining.visibility)
    }
}
