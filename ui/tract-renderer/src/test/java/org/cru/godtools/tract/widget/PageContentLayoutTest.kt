package org.cru.godtools.tract.widget

import android.app.Activity
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.ccci.gto.android.common.testing.dagger.hilt.HiltTestActivity
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class PageContentLayoutTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    private lateinit var activity: Activity

    private lateinit var layout: PageContentLayout

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(HiltTestActivity::class.java).get()

        layout = PageContentLayout(activity)
    }

    // region changeActiveCard()
    @Test
    fun `changeActiveCard() - Invalid Child View`() {
        assertThrows(IllegalArgumentException::class.java) { layout.changeActiveCard(View(activity), false) }
    }
    // endregion changeActiveCard()
}
