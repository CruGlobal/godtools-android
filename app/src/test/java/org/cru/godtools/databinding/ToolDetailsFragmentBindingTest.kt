package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.cru.godtools.ui.tooldetails.ToolDetailsActivity
import org.cru.godtools.xml.model.Manifest
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
    private val tipsManifest: Manifest = mock {
        on(this.mock.tips) doReturn mapOf(Pair("test1", mock { }))
    }
    private val emptyManifest: Manifest = mock {}

    @Before
    fun createBinding() {
        val activityController = Robolectric.buildActivity(ToolDetailsActivity::class.java)
        binding = ToolDetailsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
    }

    @Test
    fun verifyWithTips() {
        binding.manifest = MutableLiveData<Manifest>(tipsManifest)
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.actionToolTraining.visibility)
    }

    @Test
    fun verifyNoTips() {
        binding.manifest = MutableLiveData<Manifest>(emptyManifest)
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
