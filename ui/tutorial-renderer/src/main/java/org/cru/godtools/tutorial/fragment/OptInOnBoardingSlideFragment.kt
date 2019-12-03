package org.cru.godtools.tutorial.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cru.godtools.tutorial.databinding.OptinOnboardingExploreSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingMenuSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingPrepareSlideBinding
import org.cru.godtools.tutorial.databinding.OptinOnboardingTrySlideBinding

class OptInOnBoardingSlideFragment : Fragment() {

    private lateinit var callback: org.cru.godtools.tutorial.activity.OnBoardingCallbacks

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (arguments?.getInt(ARG_SLIDE_POSITION)) {
            1 -> {
                OptinOnboardingPrepareSlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            2 -> {
                OptinOnboardingTrySlideBinding.inflate(inflater, container, false).also {
                    it.callback = callback
                }.root
            }
            3 -> {
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
