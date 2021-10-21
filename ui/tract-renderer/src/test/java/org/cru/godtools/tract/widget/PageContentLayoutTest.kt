package org.cru.godtools.tract.widget

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PageContentLayoutTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private lateinit var layout: PageContentLayout

    @Before
    fun setup() {
        layout = PageContentLayout(context)
    }

    // region changeActiveCard()
    @Test
    fun `changeActiveCard() - Invalid Child View`() {
        assertThrows(IllegalArgumentException::class.java) { layout.changeActiveCard(View(context), false) }
    }
    // endregion changeActiveCard()
}
