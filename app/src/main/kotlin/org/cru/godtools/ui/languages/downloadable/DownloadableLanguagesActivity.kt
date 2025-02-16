package org.cru.godtools.ui.languages.downloadable

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

fun Context.startDownloadableLanguagesActivity() = startActivity(createDownloadableLanguagesIntent())

fun Context.createDownloadableLanguagesIntent() = Intent(this, DownloadableLanguagesActivity::class.java)
    .putExtras(BaseActivity.buildExtras(this))

@AndroidEntryPoint
class DownloadableLanguagesActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GodToolsTheme {
                DownloadableLanguagesLayout(
                    onEvent = {
                        when (it) {
                            DownloadableLanguagesEvent.NavigateUp -> finish()
                        }
                    }
                )
            }
        }
    }
}
