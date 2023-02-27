package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.BeforeTest
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.TranslationsRepositoryIT
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyTranslationsRespositoryIT : TranslationsRepositoryIT() {
    @get:Rule
    val dbRule = GodToolsDaoRule()

    override lateinit var repository: TranslationsRepository

    @BeforeTest
    fun setup() {
        repository = LegacyTranslationsRepository(dbRule.dao)
    }
}
