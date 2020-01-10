package org.cru.godtools.util

import android.widget.TextView
import org.ccci.gto.android.common.picasso.view.PicassoImageView
import org.cru.godtools.R
import org.cru.godtools.base.util.getGodToolsFile
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool

fun TextView?.bindShares(tool: Tool?) = bindShares(tool?.totalShares ?: 0)

fun TextView?.bindShares(shares: Int) =
    this?.resources?.getQuantityString(R.plurals.label_tools_shares, shares, shares)?.let { text = it }

fun PicassoImageView?.bindLocalImage(attachment: Attachment?) =
    bindLocalImage(attachment?.takeIf { it.isDownloaded }?.localFileName)

fun PicassoImageView?.bindLocalImage(filename: String?) = this?.setPicassoFile(context.getGodToolsFile(filename))
