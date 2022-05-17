package org.cru.godtools.base.tool.widget

import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.cru.godtools.tool.model.ImageScaleType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
class ScaledPicassoImageViewTest {
    private val drawable = mockk<Drawable>()
    private val imageMatrix = slot<Matrix>()
    private lateinit var view: ImageView

    private lateinit var helper: ScaledPicassoImageView.ScaleHelper

    @Before
    fun setup() {
        view = spyk(ImageView(ApplicationProvider.getApplicationContext())) {
            every { isInEditMode } returns true
        }
        every { view.drawable } returns drawable
        every { view.imageMatrix = capture(imageMatrix) } just Runs
        helper = ScaledPicassoImageView.ScaleHelper(view, null, 0, 0, mockk())
    }

    // region scaleType = MATRIX
    @Test
    fun testScaleTypeMatrixFit() {
        view.scaleType = ImageView.ScaleType.MATRIX
        helper.scaleType = ImageScaleType.FIT
        helper.gravityVertical = GravityVertical.CENTER
        helper.gravityHorizontal = GravityHorizontal.CENTER
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 70)

        helper.onSetImageDrawable()
        assertEquals(
            Matrix().apply {
                setScale(2f, 2f)
                postTranslate(0f, 15f)
            },
            imageMatrix.captured
        )
    }

    @Test
    fun testScaleTypeMatrixFitTopLeft() {
        view.scaleType = ImageView.ScaleType.MATRIX
        helper.scaleType = ImageScaleType.FIT
        helper.gravityVertical = GravityVertical.TOP
        helper.gravityHorizontal = GravityHorizontal.LEFT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 70)

        helper.onSetImageDrawable()
        assertEquals(Matrix().apply { setScale(2f, 2f) }, imageMatrix.captured)
    }

    @Test
    fun testScaleTypeMatrixFillTopRight() {
        view.scaleType = ImageView.ScaleType.MATRIX
        helper.scaleType = ImageScaleType.FILL
        helper.gravityVertical = GravityVertical.TOP
        helper.gravityHorizontal = GravityHorizontal.RIGHT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 80)

        helper.onSetImageDrawable()
        assertEquals(
            Matrix().apply {
                setScale(4f, 4f)
                postTranslate(-40f, 0f)
            },
            imageMatrix.captured
        )
    }

    @Test
    fun testScaleTypeMatrixFillXBottom() {
        view.scaleType = ImageView.ScaleType.MATRIX
        helper.scaleType = ImageScaleType.FILL_X
        helper.gravityVertical = GravityVertical.BOTTOM
        stubDrawableDimensions(20, 20)
        stubViewDimensions(40, 80)

        helper.onSetImageDrawable()
        assertEquals(
            Matrix().apply {
                setScale(2f, 2f)
                postTranslate(0f, 40f)
            },
            imageMatrix.captured
        )
    }

    @Test
    fun testScaleTypeMatrixFillYLeft() {
        view.scaleType = ImageView.ScaleType.MATRIX
        helper.scaleType = ImageScaleType.FILL_Y
        helper.gravityHorizontal = GravityHorizontal.LEFT
        stubDrawableDimensions(20, 20)
        stubViewDimensions(70, 40)

        helper.onSetImageDrawable()
        assertEquals(Matrix().apply { setScale(2f, 2f) }, imageMatrix.captured)
    }
    // endregion scaleType = MATRIX

    private fun stubDrawableDimensions(width: Int = 0, height: Int = 0) {
        every { drawable.intrinsicWidth } returns width
        every { drawable.intrinsicHeight } returns height
    }

    private fun stubViewDimensions(
        width: Int = 0,
        height: Int = 0,
        paddingLeft: Int = 0,
        paddingTop: Int = 0,
        paddingRight: Int = 0,
        paddingBottom: Int = 0
    ) {
        every { view.width } returns width
        every { view.height } returns height
        every { view.paddingLeft } returns paddingLeft
        every { view.paddingTop } returns paddingTop
        every { view.paddingRight } returns paddingRight
        every { view.paddingBottom } returns paddingBottom
    }
}
