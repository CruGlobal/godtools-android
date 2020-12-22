package org.cru.godtools.tract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.MainThread
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.EXTRA_LANGUAGE
import org.cru.godtools.base.EXTRA_TOOL
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.activity.BaseActivity
import org.cru.godtools.tract.EXTRA_MODAL
import org.cru.godtools.tract.EXTRA_PAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.databinding.TractModalActivityBinding
import org.cru.godtools.tract.ui.controller.ModalController
import org.cru.godtools.tract.ui.controller.bindController
import org.cru.godtools.xml.model.Modal
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

internal fun Activity.startModalActivity(modal: Modal) = startActivity(
    Intent(this, ModalActivity::class.java).putExtras(
        Bundle(4).apply {
            putString(EXTRA_TOOL, modal.manifest.code)
            putLocale(EXTRA_LANGUAGE, modal.manifest.locale)
            putString(EXTRA_PAGE, modal.page.id)
            putString(EXTRA_MODAL, modal.id)
        }
    ),
    ActivityOptionsCompat.makeCustomAnimation(this, R.anim.activity_fade_in, R.anim.activity_fade_out).toBundle()
)

@AndroidEntryPoint
class ModalActivity : BaseActivity<TractModalActivityBinding>(R.layout.tract_modal_activity) {
    private val dataModel: ModalActivityDataModel by viewModels()
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
        binding.modalLayout.bindController(modalControllerFactory)
            .also { dataModel.modal.observe(this, it) }
    }

    override fun onResume() {
        super.onResume()
        eventBus.register(this)
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContentEvent(event: Event) {
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

class ModalActivityDataModel @ViewModelInject constructor(manifestManager: ManifestManager) :
    LatestPublishedManifestDataModel(manifestManager) {
    val pageId = MutableLiveData<String?>()
    val modalId = MutableLiveData<String?>()

    val modal =
        manifest.combineWith(pageId.distinctUntilChanged(), modalId.distinctUntilChanged()) { manifest, page, modal ->
            manifest?.findPage(page)?.findModal(modal)
        }
}
