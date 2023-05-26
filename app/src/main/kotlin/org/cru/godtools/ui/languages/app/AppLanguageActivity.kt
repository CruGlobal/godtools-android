package org.cru.godtools.ui.languages.app

import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

@AndroidEntryPoint
class AppLanguageActivity : BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GodToolsTheme {
                AppLanguageLayout(
                    onEvent = {
                        when (it) {
                            is AppLanguageEvent.NavigateBack -> finish()
                            is AppLanguageEvent.LanguageSelected -> {
                                settings.appLanguage = it.language
                                finish()
                            }
                        }
                    }
                )
            }
        }
    }
    // endregion Lifecycle
}
