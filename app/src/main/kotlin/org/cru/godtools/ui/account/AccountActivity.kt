package org.cru.godtools.ui.account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

fun Activity.startAccountActivity() = startActivity(
    Intent(this, AccountActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
)

@AndroidEntryPoint
class AccountActivity : BaseActivity() {
    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GodToolsTheme {
                AccountLayout(onEvent = {
                    when (it) {
                        AccountLayoutEvent.ACTION_UP -> onSupportNavigateUp()
                    }
                })
            }
        }
    }
    // endregion Lifecycle
}
