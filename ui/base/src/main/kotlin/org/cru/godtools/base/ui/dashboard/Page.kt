package org.cru.godtools.base.ui.dashboard

import androidx.annotation.IdRes
import org.cru.godtools.ui.R

enum class Page(@IdRes val id: Int) {
    LESSONS(R.id.dashboard_page_lessons),
    HOME(R.id.dashboard_page_home),
    FAVORITE_TOOLS(R.id.dashboard_page_favorites),
    ALL_TOOLS(R.id.dashboard_page_all_tools);

    companion object {
        val DEFAULT = HOME

        fun findPage(@IdRes id: Int) = values().firstOrNull { it.id == id }
    }
}
