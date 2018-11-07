package org.cru.godtools.content

import android.content.Context

import org.ccci.gto.android.common.eventbus.content.CachingAsyncTaskEventBusLoader
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.content.ToolEventBusSubscriber
import org.keynote.godtools.android.db.GodToolsDao

class ToolLoader(context: Context, private val code: String) : CachingAsyncTaskEventBusLoader<Tool>(context) {
    private val dao = GodToolsDao.getInstance(context)

    init {
        addEventBusSubscriber(ToolEventBusSubscriber(this))
    }

    override fun loadInBackground(): Tool? {
        return dao.find(Tool::class.java, code)
    }
}
