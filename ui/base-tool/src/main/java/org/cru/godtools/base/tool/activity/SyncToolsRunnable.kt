package org.cru.godtools.base.tool.activity

import android.content.Context
import android.os.Bundle
import androidx.annotation.RestrictTo
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.runBlocking
import org.cru.godtools.sync.task.ToolSyncTasks
import java.io.IOException

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SyncToolsRunnable(context: Context, private val postSyncTask: Runnable) : Runnable {
    private val context: Context = context.applicationContext

    @JvmField
    val future: SettableFuture<Any> = SettableFuture.create()

    override fun run() {
        runBlocking {
            try {
                ToolSyncTasks.getInstance(context).syncTools(Bundle.EMPTY)
            } catch (ignored: IOException) {
            }
            postSyncTask.run()
            future.set(null)
        }
    }
}
