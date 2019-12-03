package org.cru.godtools.tutorial.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.databinding.OptinOnboardingExploreSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingMenuSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingPrepareSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingTrySlideBinding
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class OptInOnBoardingSlideFragment : Fragment() {

    private lateinit var callback: OnBoardingCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (arguments?.getInt(ARG_LAYOUT_ID)) {
            R.layout.optin_onboarding_prepare_slide -> {
                OptinOnboardingPrepareSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            R.layout.optin_onboarding_try_slide -> {
                OptinOnboardingTrySlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            R.layout.optin_onboarding_menu_slide -> {
                OptinOnboardingMenuSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            else -> {
                OptinOnboardingExploreSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
        }
    }

    fun setCallback(callback: OnBoardingCallbacks) {
        this.callback = callback
    }

    companion object {

        private const val ARG_LAYOUT_ID = "layout_id"

        @JvmStatic
        fun newInstance(position: Int): OptInOnBoardingSlideFragment {
            return OptInOnBoardingSlideFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LAYOUT_ID, position)
                }
            }
        }
    }
}
