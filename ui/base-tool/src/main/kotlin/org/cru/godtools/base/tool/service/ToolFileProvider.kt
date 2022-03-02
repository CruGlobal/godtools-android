package org.cru.godtools.base.tool.service

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider

private const val DISPLAYNAME_FIELD = "displayName"

class ToolFileProvider : FileProvider() {
    override fun getType(uri: Uri) = uri.getQueryParameter(DISPLAYNAME_FIELD)
        ?.substringAfterLast('.', missingDelimiterValue = "")
        ?.takeIf { it.isNotEmpty() }
        ?.let { MimeTypeMap.getSingleton().getMimeTypeFromExtension(it) }
        ?: super.getType(uri)
}
