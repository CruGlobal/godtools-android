package org.cru.godtools.tool.tract

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.cru.godtools.db.DatabaseModule
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.repository.TranslationsRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
class MockDatabaseModule {
    @get:Provides
    val languagesRepository: LanguagesRepository by lazy { mockk() }
    @get:Provides
    val toolsRepository: ToolsRepository by lazy { mockk() }
    @get:Provides
    val trainingTipsRepository: TrainingTipsRepository by lazy { mockk() }
    @get:Provides
    val translationsRepository: TranslationsRepository by lazy {
        mockk {
            every { findLatestTranslationFlow(any(), any(), any()) } returns flowOf(null)
        }
    }
}
