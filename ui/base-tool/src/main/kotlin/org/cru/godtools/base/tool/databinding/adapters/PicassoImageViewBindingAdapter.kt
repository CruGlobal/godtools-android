package org.cru.godtools.base.tool.databinding.adapters

import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import jp.wasabeef.picasso.transformations.CropTransformation.GravityHorizontal
import jp.wasabeef.picasso.transformations.CropTransformation.GravityVertical
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.model.getFileBlocking
import org.cru.godtools.base.tool.ui.util.layoutDirection
import org.cru.godtools.base.tool.widget.SimpleScaledPicassoImageView
import org.cru.godtools.base.toolFileSystem
import org.cru.godtools.shared.tool.parser.model.Gravity
import org.cru.godtools.shared.tool.parser.model.ImageScaleType
import org.cru.godtools.shared.tool.parser.model.Resource

private const val PICASSO_FILE = "picassoFile"

@BindingAdapter(PICASSO_FILE)
internal fun SimplePicassoImageView.setPicassoFile(resource: Resource?) = setPicassoResource(resource)

@BindingAdapter(PICASSO_FILE, "scaleType", "gravity")
internal fun SimpleScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scaleType: ImageScaleType?,
    gravity: Gravity?
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
    setPicassoFile(resource?.getFileBlocking(context.toolFileSystem))
