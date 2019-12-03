package org.cru.godtools.tutorial.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.databinding.BakedInOnboardingFinalBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingOthersBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingReadyBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingToolsBinding
import org.cru.godtools.tutorial.databinding.BakedInOnboardingWelcomeBinding
import org.cru.godtools.tutorial.util.OnBoardingCallbacks

class BakedInOnBoardingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return when (arguments?.getInt(ARG_LAYOUT_ID)) {
            R.layout.baked_in_onboarding_others -> BakedInOnboardingOthersBinding.inflate(
                inflater,
                container,
                false
            ).also { it.callback = callback }.root
            R.layout.baked_in_onboarding_tools -> BakedInOnboardingToolsBinding.inflate(
                inflater,
                container,
                false
            ).also { it.callback = callback }.root
            R.layout.baked_in_onboarding_ready -> BakedInOnboardingReadyBinding.inflate(
                inflater,
                container,
                false
            ).also { it.callback = callback }.root
            R.layout.baked_in_onboarding_final -> BakedInOnboardingFinalBinding.inflate(
                inflater,
                container,
                false
            ).also { it.callback = callback }.root
            else -> BakedInOnboardingWelcomeBinding.inflate(
                inflater,
                container,
                false
            ).also { it.callback = callback }.root
        }
    }

    private lateinit var callback: OnBoardingCallbacks

    fun setCallback(callback: OnBoardingCallbacks) {
        this.callback = callback
    }

    companion object {
        private const val ARG_LAYOUT_ID = "layout_id"

        @JvmStatic
        fun newInstance(layout: Int): BakedInOnBoardingFragment {
            return BakedInOnBoardingFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LAYOUT_ID, layout)
                }
            }
        }
    }
}
