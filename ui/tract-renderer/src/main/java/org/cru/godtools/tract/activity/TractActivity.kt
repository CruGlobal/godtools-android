// ktlint-disable filename
package org.cru.godtools.tract.activity

import androidx.activity.viewModels
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.tract.service.FollowupService
import javax.inject.Inject

abstract class KotlinTractActivity : BaseToolActivity(true) {
    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    protected val dataModel: TractActivityDataModel by viewModels()

    override val activeManifest get() = dataModel.activeManifest.value

    // region Share Link Logic
    override fun hasShareLinkUri() = activeManifest != null
    // endregion Share Link Logic
}
