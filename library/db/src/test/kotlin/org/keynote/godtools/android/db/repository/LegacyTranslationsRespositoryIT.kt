package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlinx.coroutines.test.StandardTestDispatcher
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.TranslationsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyTranslationsRespositoryIT : TranslationsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule(dispatcher = StandardTestDispatcher(testScope.testScheduler))

    override lateinit var repository: TranslationsRepository
    override val toolsRepository: ToolsRepository = mockk(relaxUnitFun = true)
    override val languagesRepository: LanguagesRepository = mockk(relaxUnitFun = true)

    @BeforeTest
    fun setup() {
        repository = LegacyTranslationsRepository(dbRule.dao)
    }
}
