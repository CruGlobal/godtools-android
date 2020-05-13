package org.cru.godtools.tract.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.CallSuper
import androidx.annotation.MainThread
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.observe
import butterknife.BindView
import org.ccci.gto.android.common.androidx.lifecycle.combineWith
import org.ccci.gto.android.common.util.os.getLocale
import org.ccci.gto.android.common.util.os.putLocale
import org.cru.godtools.base.Constants.EXTRA_LANGUAGE
import org.cru.godtools.base.Constants.EXTRA_TOOL
import org.cru.godtools.base.model.Event
import org.cru.godtools.base.tool.activity.ImmersiveActivity
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.tract.Constants.EXTRA_MODAL
import org.cru.godtools.tract.Constants.EXTRA_PAGE
import org.cru.godtools.tract.R
import org.cru.godtools.tract.R2
import org.cru.godtools.tract.viewmodel.ModalViewHolder
import org.cru.godtools.xml.model.Modal
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

internal fun Activity.startModalActivity(modal: Modal) = startActivity(
    Intent(this, ModalActivity::class.java).putExtras(Bundle(4).apply {
        putString(EXTRA_TOOL, modal.manifest.code)
        putLocale(EXTRA_LANGUAGE, modal.manifest.locale)
        putString(EXTRA_PAGE, modal.page.id)
        putString(EXTRA_MODAL, modal.id)
    }),
    ActivityOptionsCompat.makeCustomAnimation(this, R.anim.activity_fade_in, R.anim.activity_fade_out)
        .toBundle()
)

class ModalActivity : ImmersiveActivity(true, R.layout.activity_modal) {
    @JvmField
    @BindView(R2.id.modal_root)
    var mModalView: View? = null

    private val dataModel: ModalActivityDataModel by viewModels()

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

    @CallSuper
    override fun onContentChanged() {
        super.onContentChanged()
        setupModalViewHolder()
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

    private fun validStartState() = dataModel.toolCode.value != null

    private fun setupModalViewHolder() {
        mModalView?.let { dataModel.modal.observe(this, ModalViewHolder.forView(it)) }
    }

    private fun checkForDismissEvent(event: Event) {
        if (dataModel.modal.value?.dismissListeners?.contains(event.id) == true) finish()
    }
}

class ModalActivityDataModel @Inject constructor(manifestManager: ManifestManager) :
    LatestPublishedManifestDataModel(manifestManager) {
    val pageId = MutableLiveData<String?>()
    val modalId = MutableLiveData<String?>()

    val modal =
        manifest.combineWith(pageId.distinctUntilChanged(), modalId.distinctUntilChanged()) { manifest, page, modal ->
            manifest?.findPage(page)?.findModal(modal)
        }
}
