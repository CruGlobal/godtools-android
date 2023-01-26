package org.cru.godtools.tool.lesson

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import org.cru.godtools.db.DatabaseModule
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.keynote.godtools.android.db.GodToolsDao

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
class MockDatabaseModule {
    @get:Provides
    val dao: GodToolsDao by lazy { mockk() }
    @get:Provides
    val languagesRepository: LanguagesRepository by lazy { mockk() }
    @get:Provides
    val toolsRepository: ToolsRepository by lazy { mockk() }
}
