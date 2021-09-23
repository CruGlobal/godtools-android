package org.cru.godtools.base.tool.ui.controller

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.children
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.model.Base
import org.cru.godtools.tool.model.Image
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.Paragraph
import org.cru.godtools.tool.model.Text
import org.cru.godtools.tract.R
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
    private lateinit var cache: UiControllerCache
    private lateinit var contentContainer: LinearLayout

    private lateinit var controller: ConcreteParentController

    private val manifest: Manifest = Manifest()

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).get()
        context = ContextThemeWrapper(activity, R.style.Theme_AppCompat)
        contentContainer = LinearLayout(context)
        cache = mock {
            on { acquire(any<Text>()) } doAnswer { testController(TextView(context)) }
            on { acquire(any<Image>()) } doAnswer { testController(ImageView(context)) }
        }
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

    class ConcreteParentController(
        public override val childContainer: LinearLayout,
        cache: UiControllerCache
    ) : ParentController<Paragraph>(
        Paragraph::class,
        childContainer,
        null,
        UiControllerCache.Factory { _, _ -> cache }
    )

    private inline fun <reified T : Base> testController(root: View) = object : BaseController<T>(T::class, root) {}
}
