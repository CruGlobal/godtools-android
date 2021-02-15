package org.cru.godtools.ui.dashboard

import androidx.annotation.IdRes
import org.cru.godtools.R

internal enum class Page(@IdRes val id: Int) {
    LESSONS(R.id.action_lessons), FAVORITE_TOOLS(R.id.action_favorites), ALL_TOOLS(R.id.action_all_tools);

    companion object {
        val DEFAULT = FAVORITE_TOOLS

        fun findPage(@IdRes id: Int) = values().firstOrNull { it.id == id }
    }
}
