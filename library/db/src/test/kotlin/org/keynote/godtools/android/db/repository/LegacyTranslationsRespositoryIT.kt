package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.TranslationsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
internal class LegacyTranslationsRespositoryIT : TranslationsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule(dispatcher = StandardTestDispatcher(testScope.testScheduler))

    override lateinit var repository: TranslationsRepository

    @BeforeTest
    fun setup() {
        repository = LegacyTranslationsRepository(dbRule.dao)
    }
}
