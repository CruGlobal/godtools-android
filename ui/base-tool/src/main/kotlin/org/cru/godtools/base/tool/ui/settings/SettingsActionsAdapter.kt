package org.cru.godtools.base.tool.ui.settings

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import org.ccci.gto.android.common.androidx.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.tool.databinding.ToolSettingsItemActionBinding

class SettingsActionsAdapter(lifecycleOwner: LifecycleOwner? = null) :
    SimpleDataBindingAdapter<ToolSettingsItemActionBinding>(lifecycleOwner) {
    init {
        setHasStableIds(true)
    }

    var actions = emptyList<SettingsAction>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = actions.size
    override fun getItemId(position: Int) = actions[position].id

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        ToolSettingsItemActionBinding.inflate(LayoutInflater.from(parent.context), parent, false)

    override fun onBindViewDataBinding(binding: ToolSettingsItemActionBinding, position: Int) {
        binding.action = actions[position]
    }

    open class SettingsAction(
        val id: Long = RecyclerView.NO_ID,
        @StringRes
        val label: Int,
        @ColorRes
        val labelColor: Int? = null,
        @DrawableRes
        val icon: Int,
        @ColorRes
        val iconTint: Int? = null,
        val background: Drawable?,
        private val onClick: () -> Unit = {},
    ) {
        // HACK: this exposes the onClick lambda to Data Binding
        fun onClick() = onClick.invoke()
    }
}
