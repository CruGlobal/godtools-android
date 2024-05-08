package org.cru.godtools.init.content

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.Tasks

@Singleton
class InitialContentImporter internal constructor(tasks: Tasks, dispatcher: CoroutineDispatcher) {
    @Inject
    internal constructor(tasks: Tasks) : this(tasks, Dispatchers.IO)

    private val coroutineScope = CoroutineScope(dispatcher)

    init {
        coroutineScope.launch {
            val tools = async { tasks.loadBundledTools() }
            val languages = launch { tasks.loadBundledLanguages() }

            launch {
                tools.join()
                tasks.initFavoriteTools()
            }
            launch {
                tasks.loadBundledAttachments(tools.await())
                tasks.importBundledAttachments()
            }
            launch {
                languages.join()
                tasks.loadBundledTranslations(tools.await())
                tasks.importBundledTranslations()
            }
        }
    }
}
