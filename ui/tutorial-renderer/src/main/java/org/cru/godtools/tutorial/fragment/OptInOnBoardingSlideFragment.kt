package org.cru.godtools.tutorial.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cru.godtools.tutorial.databinding.OnboardingExploreSlideBinding
import org.cru.godtools.tutorial.databinding.OnboardingMenuSlideBinding
import org.cru.godtools.tutorial.databinding.OnboardingPrepareSlideBinding
import org.cru.godtools.tutorial.databinding.OnboardingTrySlideBinding

class OptInOnBoardingSlideFragment : Fragment() {

    private lateinit var callback: org.cru.godtools.tutorial.activity.OnBoardingCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (arguments?.getInt(ARG_SLIDE_POSITION)) {
            1 -> {
                OnboardingPrepareSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            2 -> {
                OnboardingTrySlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            3 -> {
                OnboardingMenuSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            else -> {
                OnboardingExploreSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
        }
    }

    fun setCallback(callback: org.cru.godtools.tutorial.activity.OnBoardingCallbacks) {
        this.callback = callback
    }

    companion object {

        private const val ARG_SLIDE_POSITION = "slide_position"

        @JvmStatic
        fun newInstance(position: Int): OptInOnBoardingSlideFragment {
            return OptInOnBoardingSlideFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SLIDE_POSITION, position)
                }
            }
        }
    }
}
