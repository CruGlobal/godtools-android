package org.cru.godtools.base.tool.ui.controller

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.R
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.children
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.shared.tool.parser.model.Base
import org.cru.godtools.shared.tool.parser.model.Image
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.Paragraph
import org.cru.godtools.shared.tool.parser.model.Text
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class ParentControllerTest {
    private lateinit var context: Context
    private val cache: UiControllerCache = mockk {
        every { acquire(ofType<Text>()) } answers { testController(TextView(context)) }
        every { acquire(ofType<Image>()) } answers { testController(ImageView(context)) }
        every { release(any(), any()) } just Runs
    }
    private lateinit var contentContainer: LinearLayout

    private lateinit var controller: ConcreteParentController

    private val manifest: Manifest = Manifest()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)
        contentContainer = LinearLayout(context)
        controller = ConcreteParentController(contentContainer, cache)
    }

    @Test
    fun verifyBindContent() {
        controller.model = Paragraph(manifest) { listOf(Text(it), Image(it)) }
        assertEquals(2, controller.childContainer.childCount)
        assertThat(
            controller.childContainer.children.toList(),
            contains(instanceOf(TextView::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = Paragraph(manifest) { listOf(Image(it), Text(it), Image(it)) }
        assertEquals(3, controller.childContainer.childCount)
        assertThat(
            controller.childContainer.children.toList(),
            contains(
                instanceOf(ImageView::class.java),
                instanceOf(TextView::class.java),
                instanceOf(ImageView::class.java)
            )
        )

        controller.model = null
        assertEquals(0, controller.childContainer.childCount)
    }

    @Test
    fun verifyBindContentWithUnmanagedViews() {
        controller.childContainer.addView(Space(context))

        controller.model = Paragraph(manifest) { listOf(Text(it), Image(it)) }
        assertEquals(3, controller.childContainer.childCount)
        assertThat(
            controller.childContainer.children.toList(),
            contains(instanceOf(Space::class.java), instanceOf(TextView::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = Paragraph(manifest) { listOf(Image(it)) }
        assertEquals(2, controller.childContainer.childCount)
        assertThat(
            controller.childContainer.children.toList(),
            contains(instanceOf(Space::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = null
        assertEquals(1, controller.childContainer.childCount)
        assertThat(controller.childContainer.children.toList(), contains(instanceOf(Space::class.java)))
    }

    class ConcreteParentController(public override val childContainer: LinearLayout, cache: UiControllerCache) :
        ParentController<Paragraph>(
            Paragraph::class,
            childContainer,
            null,
            UiControllerCache.Factory { _, _ -> cache }
        )

    private inline fun <reified T : Base> testController(root: View) = object : BaseController<T>(T::class, root) {}
}
