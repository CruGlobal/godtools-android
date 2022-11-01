package org.cru.godtools.base.tool.ui.shareable.model

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.Locale
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.model.getFileBlocking
import org.cru.godtools.base.tool.model.shareable.buildShareIntent
import org.cru.godtools.base.tool.ui.share.model.ShareItem
import org.cru.godtools.base.tool.ui.shareable.ShareableImageBottomSheetDialogFragment
import org.cru.godtools.shared.tool.parser.model.shareable.ShareableImage
import org.cru.godtools.tool.R

@Parcelize
class ShareableImageShareItem(
    val tool: String?,
    val locale: Locale?,
    val shareable: String?,
    val thumbnail: File?,
    override val shareIntent: Intent?
) : ShareItem {
    @AssistedInject
    internal constructor(
        @ApplicationContext context: Context,
        fileSystem: ToolFileSystem,
        @Assisted shareable: ShareableImage
    ) : this(
        shareable.manifest.code,
        shareable.manifest.locale,
        shareable.id,
        shareable.resource?.getFileBlocking(fileSystem),
        shareable.buildShareIntent(context)
    )

    @AssistedFactory
    interface Factory {
        fun create(@Assisted shareable: ShareableImage): ShareableImageShareItem
    }

    override val actionLayout get() = R.layout.tool_share_item_shareable_image

    override val order get() = Ordered.LOWEST_PRECEDENCE

    override val isValid get() = tool != null && locale != null && shareable != null

    override fun triggerAction(activity: Activity) {
        check(tool != null && locale != null && shareable != null)

        ShareableImageBottomSheetDialogFragment(tool, locale, shareable)
            .show((activity as FragmentActivity).supportFragmentManager, null)
    }
}
