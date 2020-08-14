package org.cru.godtools.databinding

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.ui.tooldetails.ToolDetailsActivity
import org.cru.godtools.ui.tooldetails.ToolDetailsFragment
import org.cru.godtools.xml.util.getXmlParserForResource
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import java.util.Locale

private const val TOOL_CODE = "test"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ToolDetailsFragmentBindingTest {
    private lateinit var binding: ToolDetailsFragmentBinding

    @Before
    fun createBinding(){
        val activityController = Robolectric.buildActivity(ToolDetailsActivity::class.java)
        binding = ToolDetailsFragmentBinding.inflate(LayoutInflater.from(activityController.get()), null, false)
    }

    @Test
    fun verifyWithTips() {
        val manifest = Manifest(TOOL_CODE, Locale.ENGLISH, getXmlParserForResource("manifest_tips_invalid.xml")){
            getXmlParserForResource(it)
        }
        binding.manifest = MutableLiveData<Manifest>(manifest)
        binding.executePendingBindings()

        assertEquals(View.VISIBLE, binding.actionToolTraining.visibility)
    }


}
