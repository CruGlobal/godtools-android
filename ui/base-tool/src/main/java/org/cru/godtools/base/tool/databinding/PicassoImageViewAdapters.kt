package org.cru.godtools.base.tool.databinding

import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.tool.model.view.ResourceViewUtils
import org.cru.godtools.xml.model.Resource

@BindingAdapter("picassoFile")
internal fun SimplePicassoImageView.setPicassoFile(resource: Resource?) = ResourceViewUtils.bind(resource, this)
