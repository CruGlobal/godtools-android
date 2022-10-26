package org.cru.godtools.base.tool.model.shareable

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.cru.godtools.base.tool.model.getFileBlocking
import org.cru.godtools.base.toolFileSystem
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage

internal fun ShareableImage.buildShareIntent(context: Context): Intent? {
    return resource?.let { resource ->
        resource.getFileBlocking(context.toolFileSystem)?.let { file ->
            val authority = "${context.packageName}.tool.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, file, resource.name.orEmpty())

            Intent(Intent.ACTION_SEND)
                .setType("image/*")
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
