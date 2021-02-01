package org.cru.godtools.base.tool.model.view

import android.view.View
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
}
