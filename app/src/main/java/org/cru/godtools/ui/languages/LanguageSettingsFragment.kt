package org.cru.godtools.ui.languages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.OnClick
import butterknife.Optional
import org.cru.godtools.R
import org.cru.godtools.activity.startLanguageSelectionActivity
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.fragment.BasePlatformFragment

class LanguageSettingsFragment : BasePlatformFragment() {
    @JvmField
    @BindView(R.id.primary_language)
    internal var primaryLanguageView: TextView? = null
    @JvmField
    @BindView(R.id.parallel_language)
    internal var parallelLanguageView: TextView? = null

    // region Lifecycle
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_language_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateLanguages()
    }

    override fun onUpdatePrimaryLanguage() {
        super.onUpdatePrimaryLanguage()
        updateLanguages()
    }

    override fun onUpdateParallelLanguage() {
        super.onUpdateParallelLanguage()
        updateLanguages()
    }
    // endregion Lifecycle

    private fun updateLanguages() {
        primaryLanguageView?.apply {
            text = primaryLanguage.getDisplayName(context, null, null)
        }
        parallelLanguageView?.apply {
            parallelLanguage?.also { text = it.getDisplayName(context, null, null) }
                ?: setText(R.string.action_language_parallel_select)
        }
    }

    @Optional
    @OnClick(R.id.primary_language)
    internal fun editPrimaryLanguage() = requireActivity().startLanguageSelectionActivity(true)

    @Optional
    @OnClick(R.id.parallel_language)
    internal fun editParallelLanguage() = requireActivity().startLanguageSelectionActivity(false)
}
