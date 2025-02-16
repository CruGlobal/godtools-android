package org.cru.godtools.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

private const val EXTRA_CREATE = "createAccount"

fun Context.startLoginActivity(createAccount: Boolean = false) = startActivity(
    Intent(this, LoginActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
        .putExtra(EXTRA_CREATE, createAccount)
)

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    @Inject
    internal lateinit var accountManager: GodToolsAccountManager

    private val createAccount get() = intent?.getBooleanExtra(EXTRA_CREATE, false) ?: false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishWhenAuthenticated()

        enableEdgeToEdge()
        setContent {
            GodToolsTheme {
                LoginLayout(
                    createAccount = createAccount,
                    onEvent = {
                        when (it) {
                            LoginLayoutEvent.Close -> finish()
                        }
                    }
                )
            }
        }
    }

    private fun finishWhenAuthenticated() {
        accountManager.isAuthenticatedFlow
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { if (it) finish() }
            .launchIn(lifecycleScope)
    }
}
