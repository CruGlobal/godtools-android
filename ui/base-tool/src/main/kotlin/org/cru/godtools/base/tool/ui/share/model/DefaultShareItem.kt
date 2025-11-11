package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.service.chooser.ChooserAction
import android.util.Log
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.base.Ordered
import org.cru.godtools.tool.R

@Parcelize
class DefaultShareItem(override val shareIntent: Intent) : ShareItem {
    override val order get() = Ordered.HIGHEST_PRECEDENCE

    override fun triggerAction(activity: Activity) {
        val chooserIntent = Intent.createChooser(shareIntent, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic().apply {
            setPendingIntentCreatorBackgroundActivityStartMode(
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
            }
            val qrCodeCustomAction = arrayOf(
                ChooserAction.Builder(
                    Icon.createWithResource(activity, R.drawable.ic_checkmark),
                    "Share QR Code",
                    PendingIntent.getActivity(
                        activity,
                        42,
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=godtools"))
                            .addCategory(Intent.CATEGORY_BROWSABLE),
                        PendingIntent.FLAG_IMMUTABLE,
                        options.toBundle()
                    )
                ).build()
            )
            chooserIntent.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, qrCodeCustomAction)
        }
        activity.startActivity(chooserIntent)
    }
}
