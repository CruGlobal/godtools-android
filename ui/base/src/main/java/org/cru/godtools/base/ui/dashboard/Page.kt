package org.cru.godtools.base.ui.dashboard

import androidx.annotation.IdRes
import org.cru.godtools.base.ui.R

enum class Page(@IdRes val id: Int) {
    LESSONS(R.id.dashboard_page_lessons),
    FAVORITE_TOOLS(R.id.dashboard_page_favorites),
    ALL_TOOLS(R.id.dashboard_page_all_tools);

    companion object {
        fun findPage(@IdRes id: Int) = values().firstOrNull { it.id == id }
    }
}
