package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.chooser.ChooserAction
import kotlinx.parcelize.Parcelize
import org.ccci.gto.android.common.base.Ordered
import org.cru.godtools.tool.R
import org.cru.godtools.qrcode.activity.QRCodeActivity

@Parcelize
class DefaultShareItem(override val shareIntent: Intent) : ShareItem {
    override val order get() = Ordered.HIGHEST_PRECEDENCE

    override fun triggerAction(activity: Activity) {
        val chooserIntent = Intent.createChooser(shareIntent, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val options = ActivityOptions.makeBasic().apply {
                setPendingIntentCreatorBackgroundActivityStartMode(
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
            }
            val urlStringForQrCode = shareIntent.getStringExtra("EXTRA_SHARE_TOOL_URL")
            val showQrCodeIntent = Intent(activity, QRCodeActivity::class.java)
                .apply {
                    putExtra("EXTRA_SHARE_URL", urlStringForQrCode)
                }
            val qrCodeCustomAction = arrayOf(
                ChooserAction.Builder(
                    Icon.createWithResource(activity, R.drawable.ic_qr_code),
                    "Share by QR Code",
                    PendingIntent.getActivity(
                        activity,
                        42,
                        showQrCodeIntent,
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                        options.toBundle()
                    )
                ).build()
            )
            chooserIntent.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, qrCodeCustomAction)
        }
        activity.startActivity(chooserIntent)
    }
}
