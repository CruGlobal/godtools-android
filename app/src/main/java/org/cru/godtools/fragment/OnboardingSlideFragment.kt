package org.cru.godtools.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cru.godtools.activity.OnBoardingCallbacks
import org.cru.godtools.databinding.OnboardingExploreSlideBinding
import org.cru.godtools.databinding.OnboardingMenuSlideBinding
import org.cru.godtools.databinding.OnboardingPrepareSlideBinding
import org.cru.godtools.databinding.OnboardingTrySlideBinding

class OnboardingSlideFragment : Fragment() {

    private lateinit var callback: OnBoardingCallbacks

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

    fun setCallback(callback: OnBoardingCallbacks) {
        this.callback = callback
    }

    companion object {

        private const val ARG_SLIDE_POSITION = "slide_position"

        @JvmStatic
        fun newInstance(position: Int): OnboardingSlideFragment {
            return OnboardingSlideFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SLIDE_POSITION, position)
                }
            }
        }
    }
}
