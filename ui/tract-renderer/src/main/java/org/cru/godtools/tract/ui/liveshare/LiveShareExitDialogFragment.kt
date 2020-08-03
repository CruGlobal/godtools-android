package org.cru.godtools.tract.ui.liveshare

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.cru.godtools.tract.R

class LiveShareExitDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setMessage(R.string.tract_live_share_exit_dialog_message)
        .setPositiveButton(R.string.tract_live_share_exit_dialog_confirm) { _, _ -> activity?.finish() }
        .setNegativeButton(R.string.tract_live_share_exit_dialog_cancel, null)
        .create()
}
