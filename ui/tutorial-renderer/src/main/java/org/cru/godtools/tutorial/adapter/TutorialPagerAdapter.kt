package org.cru.godtools.tutorial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter
import org.cru.godtools.tutorial.BR
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.animation.animateToNextText
import org.cru.godtools.tutorial.databinding.BakedInTutorialWelcomeBinding
import org.cru.godtools.tutorial.util.TutorialCallbacks

internal class TutorialPagerAdapter(private val pages: List<Page>, val callbacks: TutorialCallbacks) : PagerAdapter() {
    override fun getCount() = pages.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        return DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(container.context), pages[position].layout, container, true
        ).also {
            it.setVariable(BR.callback, callbacks)
            it.startAnimations()
        }
    }

    override fun isViewFromObject(view: View, obj: Any) = view == (obj as? ViewDataBinding)?.root

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView((obj as ViewDataBinding).root)
    }

    private fun ViewDataBinding.startAnimations() {
        (this as? BakedInTutorialWelcomeBinding)?.welcomeTextView?.animateToNextText(R.string.baked_in_welcome_helping)
    }
}
