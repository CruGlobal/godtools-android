package org.cru.godtools.ui.tooldetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager.widget.PagerAdapter
import org.cru.godtools.R
import org.cru.godtools.databinding.ToolDetailsPageDescriptionBinding
import org.cru.godtools.databinding.ToolDetailsPageLanguagesBinding

internal class ToolDetailsPagerAdapter(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val dataModel: ToolDetailsFragmentDataModel
) : PagerAdapter() {
    override fun instantiateItem(container: ViewGroup, position: Int) = when (position) {
        0 -> ToolDetailsPageDescriptionBinding.inflate(LayoutInflater.from(container.context), container, true).apply {
            tool = dataModel.tool
            translation = dataModel.primaryTranslation
        }
        1 -> ToolDetailsPageLanguagesBinding.inflate(LayoutInflater.from(container.context), container, true).apply {
            languages = dataModel.availableLanguages
        }
        else -> throw IllegalArgumentException("page $position is not a valid page")
    }.also { it.lifecycleOwner = lifecycleOwner }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView((`object` as ViewDataBinding).root)
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int) = when (position) {
        0 -> context.getString(R.string.label_tools_about)
        1 -> {
            val count = dataModel.availableLanguages.value?.size ?: 0
            context.resources.getQuantityString(R.plurals.label_tools_languages, count, count)
        }
        else -> null
    }

    override fun isViewFromObject(view: View, `object`: Any) =
        DataBindingUtil.findBinding<ViewDataBinding>(view) === `object`
}
