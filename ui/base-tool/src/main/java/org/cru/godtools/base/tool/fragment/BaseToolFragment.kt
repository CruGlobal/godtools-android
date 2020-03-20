package org.cru.godtools.base.tool.fragment

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import org.cru.godtools.base.tool.viewmodel.LatestPublishedManifestDataModel
import org.cru.godtools.base.ui.fragment.BaseFragment
import org.cru.godtools.xml.model.Manifest
import splitties.fragmentargs.arg
import java.util.Locale

abstract class BaseToolFragment<B : ViewDataBinding> : BaseFragment<B> {
    constructor(@LayoutRes layoutId: Int? = null) : super(layoutId)
    constructor(@LayoutRes layoutId: Int? = null, tool: String, locale: Locale) : super(layoutId) {
        this.tool = tool
        this.locale = locale
    }

    protected var tool by arg<String>()
    protected var locale by arg<Locale>()

    protected var manifest: Manifest? = null
        set(value) {
            field = value
            onManifestUpdated()
        }

    // region Lifecycle
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDataModel()
        toolDataModel.manifest.observe(this) { manifest = it }
    }

    @CallSuper
    protected open fun onManifestUpdated() = Unit
    // endregion Lifecycle

    // region Data Model
    protected open val toolDataModel: LatestPublishedManifestDataModel by viewModels()

    private fun setupDataModel() {
        toolDataModel.toolCode.value = tool
        toolDataModel.locale.value = locale
    }
    // endregion Data Model
}
