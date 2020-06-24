package org.cru.godtools.base.tool.databinding

import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.model.view.bindBackgroundImage
import org.cru.godtools.base.tool.model.view.setPicassoResource
import org.cru.godtools.base.tool.widget.SimpleScaledPicassoImageView
import org.cru.godtools.xml.model.ImageGravity
import org.cru.godtools.xml.model.ImageScaleType
import org.cru.godtools.xml.model.Resource

@BindingAdapter("picassoFile")
internal fun SimplePicassoImageView.setPicassoFile(resource: Resource?) = setPicassoResource(resource)

@BindingAdapter("picassoFile", "scaleType", "gravity", requireAll = false)
internal fun SimpleScaledPicassoImageView.bindScaledResource(
    resource: Resource?,
    scaleType: ImageScaleType?,
    gravity: Int?
) = bindBackgroundImage(
    resource, scaleType ?: ImageScaleType.FIT, gravity?.let { ImageGravity(it) } ?: ImageGravity.CENTER
)
