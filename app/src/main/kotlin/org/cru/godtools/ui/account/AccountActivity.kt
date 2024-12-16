package org.cru.godtools.ui.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.ui.drawer.DrawerMenuLayout

fun Context.startAccountActivity() = startActivity(
    Intent(this, AccountActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
)

@AndroidEntryPoint
class AccountActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GodToolsTheme {
                DrawerMenuLayout {
                    AccountLayout(onEvent = {
                        when (it) {
                            AccountLayoutEvent.ACTION_UP -> onSupportNavigateUp()
                        }
                    })
                }
            }
        }
    }
}
