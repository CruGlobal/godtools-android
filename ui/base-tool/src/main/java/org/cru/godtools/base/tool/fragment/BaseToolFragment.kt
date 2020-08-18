package org.cru.godtools.base.tool.fragment

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import java.util.Locale
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.fragment.BaseFragment
import splitties.fragmentargs.arg

abstract class BaseToolFragment<B : ViewBinding>(@LayoutRes contentLayoutId: Int) :
    BaseFragment<B>(contentLayoutId) {
    constructor(@LayoutRes contentLayoutId: Int, tool: String, locale: Locale) : this(contentLayoutId) {
        this.tool = tool
        this.locale = locale
    }

    private var tool by arg<String>()
    private var locale by arg<Locale>()

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
    }
    // endregion Lifecycle

    // region Data Model
    protected open val toolDataModel: LatestPublishedManifestDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.toolCode.value = tool
        toolDataModel.locale.value = locale
    }
    // endregion Data Model
}
