package org.cru.godtools.databinding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.ccci.gto.android.common.picasso.view.SimplePicassoImageView
import org.cru.godtools.base.toolFileManager
import org.cru.godtools.model.Attachment

@BindingAdapter("android:src")
internal fun SimplePicassoImageView.bindAttachment(attachment: Attachment?) = setPicassoAttachment(attachment)

private fun <T> T.setPicassoAttachment(attachment: Attachment?) where T : ImageView, T : PicassoImageView =
    setPicassoFile(attachment?.takeIf { it.isDownloaded }?.getFileBlocking(context.toolFileManager))
