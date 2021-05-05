package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.tract.databinding.TractContentInputBinding
import org.cru.godtools.xml.model.Input

class InputController private constructor(
    private val binding: TractContentInputBinding,
    parentController: BaseController<*>
) : BaseController<Input>(Input::class, binding.root, parentController) {
    @AssistedInject internal constructor(@Assisted parent: ViewGroup, @Assisted parentController: BaseController<*>) :
        this(TractContentInputBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentController)

    @AssistedFactory
    interface Factory : BaseController.Factory<InputController>

    init {
        binding.controller = this
    }

    // region Lifecycle
    override fun onBind() {
        super.onBind()
        if (binding.model?.name != model?.name) binding.error = null
        binding.model = model
    }

    fun onTextUpdated() {
        // only update if we are currently in an error state
        if (binding.layout.isErrorEnabled) onValidate()
    }

    fun onFocusChanged(hasFocus: Boolean) {
        if (!hasFocus) onValidate()

        binding.input.context.getSystemService<InputMethodManager>()?.apply {
            if (hasFocus) showSoftInput(binding.input, 0) else hideSoftInputFromWindow(binding.input.windowToken, 0)
        }
    }

    override fun onValidate() = model?.validateValue(value).also { binding.error = it } == null

    override fun onBuildEvent(builder: Event.Builder, recursive: Boolean) {
        val name = model?.name ?: return
        value?.let { builder.field(name, it) }
    }
    // endregion Lifecycle

    private val value
        get() = when (model?.type) {
            Input.Type.HIDDEN -> model?.value
            else -> binding.input.text.toString()
        }
}
