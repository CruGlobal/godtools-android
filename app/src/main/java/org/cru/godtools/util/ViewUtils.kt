package org.cru.godtools.util

import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.base.fileManager
import org.cru.godtools.model.Attachment

fun PicassoImageView?.bindLocalImage(attachment: Attachment?) =
    bindLocalImage(attachment?.takeIf { it.isDownloaded }?.localFilename)

fun PicassoImageView?.bindLocalImage(filename: String?) =
    this?.setPicassoFile(filename?.let { context.fileManager.getFileBlocking(it) })
