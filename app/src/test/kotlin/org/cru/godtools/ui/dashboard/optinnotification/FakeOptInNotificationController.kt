package org.cru.godtools.ui.dashboard.optinnotification

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeOptInNotificationController : OptInNotificationController {
    val permissionStatus = MutableStateFlow(PermissionStatus.UNDETERMINED)
    var permissionRequests = 0
        private set

    @Composable
    override fun rememberPermissionStatus(): PermissionStatus =permissionStatus.collectAsState().value

    @Composable
    override fun rememberRequestPermission(): () -> Unit = { permissionRequests++ }
}
