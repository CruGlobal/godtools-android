package org.cru.godtools.tract.ui.liveshare

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.ccci.gto.android.common.androidx.lifecycle.notNull
import org.cru.godtools.tract.R
import org.cru.godtools.tract.activity.TractActivity
import org.cru.godtools.tract.liveshare.TractPublisherController

class LiveShareStartingDialogFragment : DialogFragment() {
    private val publisherController: TractPublisherController by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAutoDismissObservers()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tract_live_share_starting)
            .setView(R.layout.tract_live_share_dialog)
            .create()
    }

    private fun startAutoDismissObservers() {
        // auto-dismiss dialog when we have publisherInfo
        publisherController.publisherInfo.let {
            liveData {
                emit(it.value)
                delay(2_000)
                emitSource(it)
            }.notNull().observe(this@LiveShareStartingDialogFragment) {
                findListener<TractActivity>()?.shareLiveShareLink()
                dismissAllowingStateLoss()
            }
        }

        // auto-dismiss dialog if we are unable to connect after 10 seconds
        lifecycleScope.launchWhenResumed {
            delay(10_000)
            context?.let { Toast.makeText(it, R.string.tract_live_share_unable_to_connect, Toast.LENGTH_LONG).show() }
            dismissAllowingStateLoss()
        }
    }
}
