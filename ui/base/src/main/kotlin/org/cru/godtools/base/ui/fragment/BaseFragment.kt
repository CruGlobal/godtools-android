package org.cru.godtools.base.ui.fragment

import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import org.ccci.gto.android.common.androidx.fragment.app.BindingFragment

abstract class BaseFragment<B : ViewBinding>(@LayoutRes contentLayoutId: Int) : BindingFragment<B>(contentLayoutId)
