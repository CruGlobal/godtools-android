package org.cru.godtools.tract.ui.controller

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.children
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.cru.godtools.tract.R
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Text
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
            on { acquire(Text::class) } doAnswer { mock { on { root } doReturn TextView(context) } }
            on { acquire(Image::class) } doAnswer { mock { on { root } doReturn ImageView(context) } }
        }
        controller = ConcreteParentController(contentContainer, cache)
    }

    @Test
    fun verifyBindContent() {
        controller.model = Paragraph(manifest) { listOf(Text(it), Image(it)) }
        assertEquals(2, controller.contentContainer.childCount)
        assertThat(
            controller.contentContainer.children.toList(),
            contains(instanceOf(TextView::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = Paragraph(manifest) { listOf(Image(it), Text(it), Image(it)) }
        assertEquals(3, controller.contentContainer.childCount)
        assertThat(
            controller.contentContainer.children.toList(),
            contains(
                instanceOf(ImageView::class.java),
                instanceOf(TextView::class.java),
                instanceOf(ImageView::class.java)
            )
        )

        controller.model = null
        assertEquals(0, controller.contentContainer.childCount)
    }

    @Test
    fun verifyBindContentWithUnmanagedViews() {
        controller.contentContainer.addView(Space(context))

        controller.model = Paragraph(manifest) { listOf(Text(it), Image(it)) }
        assertEquals(3, controller.contentContainer.childCount)
        assertThat(
            controller.contentContainer.children.toList(),
            contains(instanceOf(Space::class.java), instanceOf(TextView::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = Paragraph(manifest) { listOf(Image(it)) }
        assertEquals(2, controller.contentContainer.childCount)
        assertThat(
            controller.contentContainer.children.toList(),
            contains(instanceOf(Space::class.java), instanceOf(ImageView::class.java))
        )

        controller.model = null
        assertEquals(1, controller.contentContainer.childCount)
        assertThat(controller.contentContainer.children.toList(), contains(instanceOf(Space::class.java)))
    }

    class ConcreteParentController(
        public override val contentContainer: LinearLayout,
        cache: UiControllerCache
    ) : ParentController<Paragraph>(
        Paragraph::class,
        contentContainer,
        null,
        object : UiControllerCache.Factory {
            override fun create(parent: ViewGroup, parentController: BaseController<*>) = cache
        }
    )
}
