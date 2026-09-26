package org.cru.godtools.init.content

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.cru.godtools.db.DatabaseModule.LEGACY_DATA_MIGRATION
import org.cru.godtools.init.content.task.Tasks

@Singleton
class InitialContentImporter internal constructor(
    tasks: Tasks,
    legacyDataMigration: Job,
    dispatcher: CoroutineDispatcher,
) {
    @Inject
    internal constructor(
        tasks: Tasks,
        @Named(LEGACY_DATA_MIGRATION) legacyDataMigration: Job,
    ) : this(tasks, legacyDataMigration, Dispatchers.IO)

    private val coroutineScope = CoroutineScope(dispatcher)

    init {
        coroutineScope.launch {
            // the legacy data migration inserts user data into the same tables, so it needs to finish first
            legacyDataMigration.join()

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
