package org.cru.godtools.base.tool.ui.share.model

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.chooser.ChooserAction
import androidx.core.content.ContextCompat.getString
import kotlinx.parcelize.Parcelize
import org.cru.godtools.base.ui.createQrCodeActivityPendingIntent
import org.cru.godtools.tool.R

@Parcelize
class DefaultShareItem(override val shareIntent: Intent, val urlStringForQrCode: String? = null) : ShareItem {
    override fun triggerAction(activity: Activity) {
        val chooserIntent = Intent.createChooser(shareIntent, null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && urlStringForQrCode != null) {
            val qrCodeCustomAction = arrayOf(
                ChooserAction.Builder(
                    Icon.createWithResource(activity, R.drawable.ic_qr_code),
                    getString(activity, R.string.share_action_qr_code),
                    activity.createQrCodeActivityPendingIntent(urlStringForQrCode),
                ).build()
            )
            chooserIntent.putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, qrCodeCustomAction)
        }
        activity.startActivity(chooserIntent)
    }
}
