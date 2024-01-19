package org.cru.godtools.tool.article

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.cru.godtools.db.DatabaseModule
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
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
    val toolsRepository: ToolsRepository by lazy {
        mockk {
            coEvery { updateToolViews(any(), any()) } just Runs
        }
    }
    @get:Provides
    val translationsRepository: TranslationsRepository by lazy { mockk() }
}
