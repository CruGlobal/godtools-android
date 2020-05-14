// ktlint-disable filename
package org.cru.godtools.tract.activity

import androidx.activity.viewModels
import androidx.lifecycle.observe
import org.cru.godtools.base.tool.activity.BaseToolActivity
import org.cru.godtools.base.tool.model.view.ManifestViewUtils
import org.cru.godtools.tract.databinding.TractActivityBinding
import org.cru.godtools.tract.service.FollowupService
import org.cru.godtools.xml.model.Manifest
import javax.inject.Inject

abstract class KotlinTractActivity : BaseToolActivity(true) {
    // Inject the FollowupService to ensure it is running to capture any followup forms
    @Inject
    internal lateinit var followupService: FollowupService

    protected val dataModel: TractActivityDataModel by viewModels()

    // region Lifecycle
    override fun onContentChanged() {
        super.onContentChanged()
        setupBackground()
    }
    // endregion Lifecycle

    override val activeManifest get() = dataModel.activeManifest.value

    // region UI
    // region View Binding
    protected lateinit var binding: TractActivityBinding
    // endregion View Binding

    private fun setupBackground() {
        dataModel.activeManifest.observe(this) {
            window.decorView.setBackgroundColor(Manifest.getBackgroundColor(it))
            ManifestViewUtils.bindBackgroundImage(it, binding.mainContent.backgroundImage)
        }
    }
    // endregion UI

    // region Share Link Logic
    override fun hasShareLinkUri() = activeManifest != null
    // endregion Share Link Logic
}
