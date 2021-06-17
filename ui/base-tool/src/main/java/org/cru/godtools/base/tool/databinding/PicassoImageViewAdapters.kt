package org.cru.godtools.base.tool.databinding

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.widget.SimpleScaledPicassoImageView
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.tool.model.ImageGravity
import org.cru.godtools.tool.model.ImageScaleType
import org.cru.godtools.tool.model.Resource
import org.cru.godtools.xml.model.layoutDirection

private const val PICASSO_FILE = "picassoFile"

@BindingAdapter(PICASSO_FILE)
internal fun SimplePicassoImageView.setPicassoFile(resource: Resource?) = setPicassoResource(resource)

@BindingAdapter(PICASSO_FILE, "scaleType", "gravity")
internal fun SimpleScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scaleType: ImageScaleType?,
    gravity: Int
) = bindScaledResource(resource, scaleType, ImageGravity(gravity))

private fun SimpleScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scaleType: ImageScaleType? = this.scaleType,
    gravity: ImageGravity? = null
) {
    toggleBatchUpdates(true)
    setPicassoResource(resource)
    this.scaleType = scaleType
    gravity?.let {
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
    }
    toggleBatchUpdates(false)
}

private fun <T> T.setPicassoResource(resource: Resource?) where T : ImageView, T : PicassoImageView =
    setPicassoFile(resource?.localName?.let { context.toolFileManager.getFileBlocking(it) })
