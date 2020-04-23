package org.cru.godtools.init.content

import android.content.Context
import android.os.AsyncTask
import org.cru.godtools.init.content.task.InitialContentTasks
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialContentImporter @Inject internal constructor(context: Context) {
    init {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(InitialContentTasks(context))
    }
}
