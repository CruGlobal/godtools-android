package org.cru.godtools.base.tool.fragment

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import java.util.Locale
import org.ccci.gto.android.common.androidx.fragment.app.BindingFragment
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import splitties.fragmentargs.arg

abstract class BaseToolFragment<B : ViewBinding>(@LayoutRes contentLayoutId: Int) :
    BindingFragment<B>(contentLayoutId) {
    constructor(@LayoutRes contentLayoutId: Int, tool: String, locale: Locale) : this(contentLayoutId) {
        this.tool = tool
        this.locale = locale
    }

    protected var tool by arg<String>()
        private set
    protected var locale by arg<Locale>()
        private set

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
