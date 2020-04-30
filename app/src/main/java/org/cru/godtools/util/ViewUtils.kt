package org.cru.godtools.util

import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.util.getGodToolsFile
import org.cru.godtools.model.Attachment

fun PicassoImageView?.bindLocalImage(attachment: Attachment?) =
    bindLocalImage(attachment?.takeIf { it.isDownloaded }?.localFileName)

fun PicassoImageView?.bindLocalImage(filename: String?) = this?.setPicassoFile(context.getGodToolsFile(filename))
