package org.cru.godtools.base.tool.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView
import org.cru.godtools.base.tool.widget.SimpleScaledPicassoImageView
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Resource
import org.cru.godtools.xml.model.layoutDirection

@BindingAdapter("picassoFile")
internal fun SimplePicassoImageView.setPicassoFile(resource: Resource?) = setPicassoResource(resource)

@BindingAdapter("picassoFile", "scaleType", "gravity", requireAll = false)
internal fun SimpleScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scaleType: ImageScaleType?,
    gravity: Int?
) = bindScaledResource(
    resource, scaleType ?: ImageScaleType.FIT, gravity?.let { ImageGravity(it) } ?: ImageGravity.CENTER
)

private fun ScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scale: ImageScaleType = scaleType,
    gravity: ImageGravity?
) {
    toggleBatchUpdates(true)
    asImageView().visibility = if (resource != null) View.VISIBLE else View.GONE
    setPicassoResource(resource)
    scaleType = scale
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

private fun PicassoImageView.setPicassoResource(resource: Resource?) =
    setPicassoFile(resource?.localName?.let { context.toolFileManager.getFileBlocking(it) })
