package org.cru.godtools.init.content

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cru.godtools.init.content.task.Tasks

@Singleton
class InitialContentImporter internal constructor(tasks: Tasks, dispatcher: CoroutineDispatcher) {
    @Inject
    internal constructor(tasks: Tasks) : this(tasks, Dispatchers.IO)

    private val coroutineScope = CoroutineScope(dispatcher)

    init {
        coroutineScope.launch {
            val bundledData = tasks.bundledData()

            val languages = launch { tasks.loadBundledLanguages() }
            tasks.loadBundledTools(bundledData)

            launch {
                tasks.loadBundledAttachments(bundledData)
                tasks.importBundledAttachments()
            }

            languages.join()
            tasks.loadBundledTranslations(bundledData)
            launch { tasks.importBundledTranslations() }

            tasks.initFavoriteTools(bundledData)
        }
    }
}
