package org.cru.godtools.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import org.cru.godtools.R
import org.cru.godtools.download.manager.GodToolsDownloadManager
import splitties.fragmentargs.arg
import javax.inject.Inject

class RemoveFavoriteConfirmationDialogFragment() : DialogFragment() {
    constructor(code: String, name: String) : this() {
        this.code = code
        this.name = name
    }

    private var code: String by arg()
    private var name: String by arg()

    @Inject
    internal lateinit var downloadManager: GodToolsDownloadManager

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.tools_list_remove_favorite_dialog_title, name))
            .setPositiveButton(R.string.tools_list_remove_favorite_dialog_confirm) { _, _ ->
                downloadManager.removeTool(code)
            }
            .setNegativeButton(R.string.tools_list_remove_favorite_dialog_dismiss, null)
            .create()
    }
}
