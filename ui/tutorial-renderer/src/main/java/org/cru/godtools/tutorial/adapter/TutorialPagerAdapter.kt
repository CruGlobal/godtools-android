package org.cru.godtools.tutorial.adapter

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.TutorialPageFragment

@SuppressLint("WrongConstant")
internal class TutorialPagerAdapter(
    fragmentManager: FragmentManager,
    private val pages: List<Page>
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount() = pages.size
    override fun getItem(position: Int) = TutorialPageFragment(pages[position])
}
