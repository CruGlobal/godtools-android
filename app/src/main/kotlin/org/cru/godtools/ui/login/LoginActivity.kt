package org.cru.godtools.ui.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.base.ui.theme.GodToolsTheme

fun Context.startLoginActivity() = startActivity(
    Intent(this, LoginActivity::class.java)
        .putExtras(BaseActivity.buildExtras(this))
)

@AndroidEntryPoint
class LoginActivity : BaseActivity() {
    @Inject
    internal lateinit var accountManager: GodToolsAccountManager
    private lateinit var loginState: GodToolsAccountManager.LoginState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginState = accountManager.prepareForLogin(this)
        finishWhenAuthenticated()

        setContent {
            GodToolsTheme {
                LoginLayout(
                    onEvent = {
                        when (it) {
                            is LoginLayoutEvent.Login ->
                                lifecycleScope.launch { accountManager.login(it.type, loginState) }
                            LoginLayoutEvent.Close -> finish()
                        }
                    }
                )
            }
        }
    }

    private fun finishWhenAuthenticated() {
        accountManager.isAuthenticatedFlow()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .onEach { if (it) finish() }
            .launchIn(lifecycleScope)
    }
}
