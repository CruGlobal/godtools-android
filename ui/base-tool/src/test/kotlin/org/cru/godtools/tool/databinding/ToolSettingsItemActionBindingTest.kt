package org.cru.godtools.tool.databinding

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.cru.godtools.base.tool.ui.settings.SettingsActionsAdapter
import org.cru.godtools.tool.R
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf

@RunWith(AndroidJUnit4::class)
class ToolSettingsItemActionBindingTest {
    private lateinit var context: Context
    private lateinit var binding: ToolSettingsItemActionBinding

    @BeforeTest
    fun createBinding() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        context = ContextThemeWrapper(activity, androidx.appcompat.R.style.Theme_AppCompat)

        binding = ToolSettingsItemActionBinding.inflate(LayoutInflater.from(context), null, false)
        binding.lifecycleOwner = activity
        binding.executePendingBindings()
    }

    @Test
    fun `View icon - action=null`() {
        binding.action = null
        binding.executePendingBindings()

        assertNull(binding.icon.drawable)
        assertNull(binding.icon.imageTintList)
    }

    @Test
    fun `View icon - action_iconTint=null`() {
        binding.action = SettingsActionsAdapter.SettingsAction(
            label = R.string.menu_settings,
            icon = R.drawable.ic_settings,
            background = null
        )
        binding.executePendingBindings()

        assertEquals(R.drawable.ic_settings, shadowOf(binding.icon.drawable).createdFromResId)
        assertNull(binding.icon.imageTintList)
    }

    @Test
    fun `View label - action=null`() {
        binding.action = null
        binding.executePendingBindings()

        assertEquals(
            context.getColor(androidx.appcompat.R.color.primary_text_default_material_light),
            binding.label.textColors.defaultColor
        )
    }
}
