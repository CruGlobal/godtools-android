package org.cru.godtools.ui.dashboard

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.ccci.gto.android.common.androidx.fragment.app.findListener
import org.cru.godtools.R
import splitties.fragmentargs.arg

class RemoveFavoriteConfirmationDialogFragment() : DialogFragment() {
    interface Callbacks {
        fun removeFavorite(code: String)
    }

    constructor(code: String, name: String) : this() {
        this.code = code
        this.name = name
    }

    private var code: String by arg()
    private var name: String by arg()

    override fun onCreateDialog(savedInstanceState: Bundle?) = MaterialAlertDialogBuilder(requireContext())
        .setTitle(getString(R.string.tools_list_remove_favorite_dialog_title, name))
        .setPositiveButton(R.string.tools_list_remove_favorite_dialog_confirm) { _, _ ->
            findListener<Callbacks>()?.removeFavorite(code)
        }
        .setNegativeButton(R.string.tools_list_remove_favorite_dialog_dismiss, null)
        .create()
}
