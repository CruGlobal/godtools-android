package org.cru.godtools.qrcode.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.example.qr_code.ui.theme.GodtoolsTheme
import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import androidx.core.graphics.createBitmap

fun Context.startQrCodeActivity(shareUrl: String) = startActivity(
    Intent(this, QRCodeActivity::class.java).apply {
        putExtra("EXTRA_SHARE_URL", shareUrl)
    }
)

class QRCodeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val url = intent.getStringExtra("EXTRA_SHARE_URL") ?: "https://godtoolsapp.com/tools"

        setContent {
            GodtoolsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    QRCodeScreen(
                        url = url,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(1f)
                            .clickable {
                                finish()
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun QRCodeScreen(url: String, modifier: Modifier) {
    val bitmap = remember(url) { generateQRCode(url) }
    if (bitmap != null) {
        Box(
            modifier = modifier.wrapContentSize(Alignment.Center)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier
                    .aspectRatio(1f)
            )
            Text(text = url, modifier = Modifier.align(Alignment.BottomCenter))
        }
    } else {
        return
    }
}

fun generateQRCode(url: String): Bitmap? {
    try {
        val bitMatrix = QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, 250, 250)
        val qrCodeWidth: Int = bitMatrix.width
        val qrCodeHeight: Int = bitMatrix.height

        val bitmap = createBitmap(qrCodeWidth, qrCodeHeight, Bitmap.Config.RGB_565)
        for (i in 0 until qrCodeWidth) {
            for (j in 0 until qrCodeHeight) {
                val color = if (bitMatrix.get(i, j)) -0x1000000 else -0x1 // needs Int?
                bitmap.setPixel(i, j, color)
            }
        }
        return bitmap
    } catch (e: Exception) {
        println(e)
        return null
    }
}

