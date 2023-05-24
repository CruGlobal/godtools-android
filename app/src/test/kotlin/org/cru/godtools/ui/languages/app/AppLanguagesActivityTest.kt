package org.cru.godtools.ui.languages.app

import android.app.Application
import android.content.ComponentName
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import org.cru.godtools.base.ui.startAppLanguageActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class AppLanguagesActivityTest {
    @Test
    @Config(application = Application::class)
    fun `startAppLanguageActivity()`() {
        val intent = slot<Intent>()
        val activity = spyk(Robolectric.buildActivity(AppCompatActivity::class.java).get()) {
            every { startActivity(capture(intent)) } just Runs
        }

        activity.startAppLanguageActivity()
        verify { activity.startActivity(any()) }
        assertEquals(ComponentName(activity, AppLanguageActivity::class.java), intent.captured.component)
    }
}
