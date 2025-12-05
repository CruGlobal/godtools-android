package org.cru.godtools.qrcode.activity

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import org.cru.godtools.base.ui.EXTRA_SHARE_URL
import org.cru.godtools.base.ui.theme.GodToolsTheme
import timber.log.Timber

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val url = intent.getStringExtra(EXTRA_SHARE_URL)
        if (url == null) {
            finish()
            return
        }

        setContent {
            GodToolsTheme {
                QRCodeScreen(
                    url = url,
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .clickable {
                            finish()
                        }
                )
            }
        }
    }
}

@Composable
fun QRCodeScreen(url: String, modifier: Modifier = Modifier) {
    val bitmapState = produceState<Bitmap?>(
        initialValue = null,
        key1 = url
    ) {
        value = generateQRCode(url)
    }
    val bitmap = bitmapState.value
    if (bitmap != null) {
        Box(
            modifier = modifier.wrapContentSize(Alignment.Center)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.aspectRatio(1f)
            )
            Text(text = url, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

private suspend fun generateQRCode(url: String): Bitmap? {
    try {
        val bitMatrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 250, 250)
        val qrCodeWidth: Int = bitMatrix.width
        val qrCodeHeight: Int = bitMatrix.height

        val bitmap = createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.RGB_565)
        for (i in 0 until qrCodeWidth) {
            for (j in 0 until qrCodeHeight) {
                val color = if (bitMatrix.get(i, j)) Color.BLACK else Color.WHITE
                bitmap.setPixel(i, j, color)
            }
        }
        return bitmap
    } catch (e: WriterException) {
        Timber.tag("QRCodeActivity").e(e, "Error generating QR Code")
        return null
    }
}
