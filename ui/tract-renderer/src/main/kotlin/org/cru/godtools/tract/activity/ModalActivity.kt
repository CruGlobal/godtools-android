package org.cru.godtools.tract.activity

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.fluidsonic.locale.toPlatform
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_PAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.tool.viewmodel.ToolStateHolder
import org.cru.godtools.base.ui.activity.BaseBindingActivity
import org.cru.godtools.shared.tool.parser.model.tract.Modal
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.tool.tract.R
import org.cru.godtools.tool.tract.databinding.TractModalActivityBinding
import org.cru.godtools.tract.EXTRA_MODAL
import org.cru.godtools.tract.ui.controller.ModalController
import org.cru.godtools.tract.ui.controller.bindController
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

internal fun Activity.startModalActivity(modal: Modal) = startActivity(
    Intent(this, ModalActivity::class.java).putExtras(
        Bundle(4).apply {
            putString(EXTRA_TOOL, modal.manifest.code)
            putLocale(EXTRA_LANGUAGE, modal.manifest.locale?.toPlatform())
            putString(EXTRA_PAGE, modal.page.id)
            putString(EXTRA_MODAL, modal.id)
        }
    ),
    ActivityOptions.makeCustomAnimation(
        this,
        org.cru.godtools.ui.R.anim.activity_fade_in,
        org.cru.godtools.ui.R.anim.activity_fade_out
    ).toBundle()
)

@AndroidEntryPoint
class ModalActivity : BaseBindingActivity<TractModalActivityBinding>(R.layout.tract_modal_activity) {
    private val dataModel: ModalActivityDataModel by viewModels()
    private val toolState: ToolStateHolder by viewModels()
    @Inject
    internal lateinit var modalControllerFactory: ModalController.Factory

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent?.extras?.let { extras ->
            dataModel.toolCode.value = extras.getString(EXTRA_TOOL)
            dataModel.locale.value = extras.getLocale(EXTRA_LANGUAGE)
            dataModel.pageId.value = extras.getString(EXTRA_PAGE)
            dataModel.modalId.value = extras.getString(EXTRA_MODAL)
        }

        // finish if this activity is in an invalid state
        if (!validStartState()) {
            finish()
            return
        }
        dataModel.modal.observe(this) { if (it == null) finish() }
    }

    override fun onBindingChanged() {
        binding.modalLayout.bindController(modalControllerFactory, this, toolState.toolState)
            .also { dataModel.modal.observe(this, it) }
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContentEvent(event: Event) {
        if (event.tool != dataModel.toolCode.value && event.locale != dataModel.locale.value) return
        checkForDismissEvent(event)
    }

    override fun onPause() {
        super.onPause()
        eventBus.unregister(this)
    }
    // endregion Lifecycle

    override val toolbar: Toolbar? get() = null

    private fun validStartState() = dataModel.toolCode.value != null

    private fun checkForDismissEvent(event: Event) {
        if (dataModel.modal.value?.dismissListeners?.contains(event.id) == true) finish()
    }
}

@HiltViewModel
class ModalActivityDataModel @Inject constructor(manifestManager: ManifestManager) :
    LatestPublishedManifestDataModel(manifestManager) {
    val pageId = MutableLiveData<String?>()
    val modalId = MutableLiveData<String?>()

    val modal =
        manifest.combineWith(pageId.distinctUntilChanged(), modalId.distinctUntilChanged()) { manifest, page, modal ->
            (manifest?.findPage(page) as? TractPage)?.findModal(modal)
        }
}
