package org.cru.godtools.tutorial.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.viewpager.widget.PagerAdapter
import org.cru.godtools.tutorial.BR
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.animation.animateToNextText
import org.cru.godtools.tutorial.databinding.BakedInOnboardingWelcomeBinding
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class OnBoardingPagerAdapter(val callbacks: OnBoardingCallbacks) : PagerAdapter() {

    lateinit var onBoardingPagesLayout: List<Int>

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutID = onBoardingPagesLayout[position]
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(container.context),
            layoutID,
            container,
            false
        ).also { it.setVariable(BR.callback, callbacks) }
        container.addView(binding.root)
        binding.setDataBindingAnimation()
        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = view == `object`

    override fun getCount(): Int = onBoardingPagesLayout.size

    private fun ViewDataBinding.setDataBindingAnimation() {
        when (this) {
            is BakedInOnboardingWelcomeBinding -> {
                welcomeTextView.postDelayed(
                    { welcomeTextView.animateToNextText(R.string.baked_in_welcome_helping) },
                    3000
                )
            }
        }
    }
}
