package org.cru.godtools.base.ui.fragment

import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit

@Deprecated("This will be in gto-support 3.6.2+")
fun DialogFragment.showAllowingStateLoss(manager: FragmentManager, tag: String?) {
    manager.commit(true) { add(this@showAllowingStateLoss, tag) }
}
