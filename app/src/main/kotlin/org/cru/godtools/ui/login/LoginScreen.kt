package org.cru.godtools.ui.login

import com.slack.circuit.runtime.screen.ParcelableScreen
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginScreen(val createAccount: Boolean = false) : ParcelableScreen
