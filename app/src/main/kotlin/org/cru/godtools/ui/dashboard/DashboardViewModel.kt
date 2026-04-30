package org.cru.godtools.ui.dashboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.cru.godtools.ui.dashboard.optinnotification.PermissionStatus

class DashboardViewModel : ViewModel() {
    // region optInNotification logic
    var permissionStatus: PermissionStatus? = null
        private set

    fun setPermissionStatus(status: PermissionStatus) {
        permissionStatus = status
    }

    private val _showOptInNotification = MutableStateFlow(false)
    val showOptInNotification = _showOptInNotification.asStateFlow()

    fun setShowOptInNotification(bool: Boolean) {
        _showOptInNotification.value = bool
    }
    // endregion optInNotification logic
}
