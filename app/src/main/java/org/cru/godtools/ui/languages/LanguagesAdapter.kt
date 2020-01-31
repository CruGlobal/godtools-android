package org.cru.godtools.ui.languages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ObservableField
import androidx.recyclerview.widget.RecyclerView.NO_ID
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.cru.godtools.databinding.ListItemLanguageBinding
import org.cru.godtools.model.Language
import java.util.Locale

class LanguagesAdapter(private val showNone: Boolean) : SimpleDataBindingAdapter<ListItemLanguageBinding>(),
    LanguageSelectedListener {
    init {
        setHasStableIds(true)
    }

    var callbacks: LocaleSelectedListener? = null
    var languages: List<Language>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    val selected = ObservableField<Locale?>()

    private var disabled: Set<Locale> = emptySet()
    fun setDisabled(vararg disabled: Locale) {
        this.disabled = disabled.toSet()
    }

    private fun getLanguage(position: Int) = when {
        showNone && position == 0 -> null
        else -> languages?.get(position - (if (showNone) 1 else 0))
    }

    override fun getItemCount() = (languages?.size ?: 0) + if (showNone) 1 else 0
    override fun getItemId(position: Int) = getLanguage(position)?.id ?: NO_ID

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): ListItemLanguageBinding =
        ListItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false).also {
            it.listener = this
            it.selected = selected
        }

    override fun onBindViewDataBinding(binding: ListItemLanguageBinding, position: Int) {
        binding.language = getLanguage(position)
    }

    override fun onLanguageSelected(language: Language?) {
        val locale = language?.code
        if (!disabled.contains(locale)) {
            callbacks?.onLocaleSelected(locale)
        }
    }
}
