package org.cru.godtools.base.tool.widget

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.cru.godtools.tool.model.ImageScaleType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@RunWith(AndroidJUnit4::class)
class ScaledPicassoImageViewTest {
    private lateinit var drawable: Drawable
    private lateinit var view: ImageView

    private lateinit var helper: ScaledPicassoImageView.ScaleHelper

    @Before
    fun setup() {
        drawable = mock()
        view = mock {
            on { context } doReturn ApplicationProvider.getApplicationContext()
            on { isInEditMode } doReturn true
            on { drawable } doReturn drawable
        }
        helper = ScaledPicassoImageView.ScaleHelper(view, null, 0, 0, mock())
    }

    // region scaleType = MATRIX
    @Test
    fun testScaleTypeMatrixFit() {
        view.stub { on { scaleType } doReturn ImageView.ScaleType.MATRIX }
        helper.scaleType = ImageScaleType.FIT
        helper.gravityVertical = GravityVertical.CENTER
        helper.gravityHorizontal = GravityHorizontal.CENTER
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 70)

        helper.onSetImageDrawable()
        argumentCaptor<Matrix> {
            verify(view).imageMatrix = capture()
            assertEquals(
                Matrix().apply {
                    setScale(2f, 2f)
                    postTranslate(0f, 15f)
                },
                firstValue
            )
        }
    }

    @Test
    fun testScaleTypeMatrixFitTopLeft() {
        view.stub { on { scaleType } doReturn ImageView.ScaleType.MATRIX }
        helper.scaleType = ImageScaleType.FIT
        helper.gravityVertical = GravityVertical.TOP
        helper.gravityHorizontal = GravityHorizontal.LEFT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 70)

        helper.onSetImageDrawable()
        argumentCaptor<Matrix> {
            verify(view).imageMatrix = capture()
            assertEquals(Matrix().apply { setScale(2f, 2f) }, firstValue)
        }
    }

    @Test
    fun testScaleTypeMatrixFillTopRight() {
        view.stub { on { scaleType } doReturn ImageView.ScaleType.MATRIX }
        helper.scaleType = ImageScaleType.FILL
        helper.gravityVertical = GravityVertical.TOP
        helper.gravityHorizontal = GravityHorizontal.RIGHT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 80)

        helper.onSetImageDrawable()
        argumentCaptor<Matrix> {
            verify(view).imageMatrix = capture()
            assertEquals(
                Matrix().apply {
                    setScale(4f, 4f)
                    postTranslate(-40f, 0f)
                },
                firstValue
            )
        }
    }

    @Test
    fun testScaleTypeMatrixFillXBottom() {
        view.stub { on { scaleType } doReturn ImageView.ScaleType.MATRIX }
        helper.scaleType = ImageScaleType.FILL_X
        helper.gravityVertical = GravityVertical.BOTTOM
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 80)

        helper.onSetImageDrawable()
        argumentCaptor<Matrix> {
            verify(view).imageMatrix = capture()
            assertEquals(
                Matrix().apply {
                    setScale(2f, 2f)
                    postTranslate(0f, 40f)
                },
                firstValue
            )
        }
    }

    @Test
    fun testScaleTypeMatrixFillYLeft() {
        view.stub { on { scaleType } doReturn ImageView.ScaleType.MATRIX }
        helper.scaleType = ImageScaleType.FILL_Y
        helper.gravityHorizontal = GravityHorizontal.LEFT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(70, 40)

        helper.onSetImageDrawable()
        argumentCaptor<Matrix> {
            verify(view).imageMatrix = capture()
            assertEquals(Matrix().apply { setScale(2f, 2f) }, firstValue)
        }
    }
    // endregion scaleType = MATRIX

    private fun stubDrawableDimensions(width: Int = 0, height: Int = 0) = drawable.stub {
        on { intrinsicWidth } doReturn width
        on { intrinsicHeight } doReturn height
    }

    private fun stubViewDimensions(
        width: Int = 0,
        height: Int = 0,
        paddingLeft: Int = 0,
        paddingTop: Int = 0,
        paddingRight: Int = 0,
        paddingBottom: Int = 0
    ) = view.stub {
        on { this.width } doReturn width
        on { this.height } doReturn height
        on { this.paddingLeft } doReturn paddingLeft
        on { this.paddingTop } doReturn paddingTop
        on { this.paddingRight } doReturn paddingRight
        on { this.paddingBottom } doReturn paddingBottom
    }
}
