package org.cru.godtools.base.tool.model.view

import android.content.Context
import org.cru.godtools.base.tool.widget.ScaledPicassoImageView
import org.cru.godtools.base.ui.util.getTypeface
import org.cru.godtools.xml.model.Manifest
import org.cru.godtools.xml.model.backgroundImageGravity
import org.cru.godtools.xml.model.backgroundImageScaleType

fun Manifest.getTypeface(context: Context) = context.getTypeface(locale)

fun ScaledPicassoImageView.bindBackgroundImage(manifest: Manifest?) = bindBackgroundImage(
    manifest?.backgroundImage,
    manifest.backgroundImageScaleType,
    manifest.backgroundImageGravity
)
