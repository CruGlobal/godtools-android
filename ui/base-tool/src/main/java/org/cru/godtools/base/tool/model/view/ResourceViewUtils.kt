package org.cru.godtools.base.tool.model.view

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.fileManager
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Resource
import org.cru.godtools.xml.model.layoutDirection

fun PicassoImageView.setPicassoResource(resource: Resource?) =
    setPicassoFile(resource?.localName?.let { context.fileManager.getFileBlocking(it) })

fun ScaledPicassoImageView.bindBackgroundImage(resource: Resource?, scale: ImageScaleType, gravity: Int) =
    bindBackgroundImage(resource, scale, ImageGravity(gravity))

@JvmSynthetic
fun ScaledPicassoImageView.bindBackgroundImage(resource: Resource?, scale: ImageScaleType, gravity: ImageGravity) {
    val view = asImageView()

    // update the background image itself
    toggleBatchUpdates(true)
    view.visibility = if (resource != null) View.VISIBLE else View.GONE
    setPicassoResource(resource)
    setScaleType(scale)
    val ltr = resource.layoutDirection != View.LAYOUT_DIRECTION_RTL
    setGravityHorizontal(
        when {
            gravity.isStart -> if (ltr) GravityHorizontal.LEFT else GravityHorizontal.RIGHT
            gravity.isEnd -> if (ltr) GravityHorizontal.RIGHT else GravityHorizontal.LEFT
            else -> GravityHorizontal.CENTER
        }
    )
    setGravityVertical(
        when {
            gravity.isTop -> GravityVertical.TOP
            gravity.isBottom -> GravityVertical.BOTTOM
            else -> GravityVertical.CENTER
        }
    )
    toggleBatchUpdates(false)

    // update layout params
    view.layoutParams = view.layoutParams.apply {
        when (this) {
            is FrameLayout.LayoutParams -> {
                val horizontal = when {
                    gravity.isStart -> Gravity.START
                    gravity.isEnd -> Gravity.END
                    gravity.isCenterX -> Gravity.CENTER_HORIZONTAL
                    else -> Gravity.NO_GRAVITY
                }
                val vertical = when {
                    gravity.isTop -> Gravity.TOP
                    gravity.isBottom -> Gravity.BOTTOM
                    gravity.isCenterY -> Gravity.CENTER_VERTICAL
                    else -> Gravity.NO_GRAVITY
                }
                this.gravity = horizontal.or(vertical)
            }
            is RelativeLayout.LayoutParams -> {
                // set default layout for background image first
                addRule(RelativeLayout.ALIGN_PARENT_START, 0)
                addRule(RelativeLayout.ALIGN_PARENT_END, 0)
                addRule(RelativeLayout.CENTER_HORIZONTAL, 0)
                addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0)
                addRule(RelativeLayout.CENTER_VERTICAL, 0)

                // update gravity (X-Axis)
                when {
                    gravity.isStart -> addRule(RelativeLayout.ALIGN_PARENT_START)
                    gravity.isEnd -> addRule(RelativeLayout.ALIGN_PARENT_END)
                    gravity.isCenterX -> addRule(RelativeLayout.CENTER_HORIZONTAL)
                }

                // update gravity (Y-Axis)
                when {
                    gravity.isTop -> addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    gravity.isBottom -> addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    gravity.isCenterY -> addRule(RelativeLayout.CENTER_VERTICAL)
                }
            }
        }
    }
}
