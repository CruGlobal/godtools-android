package org.keynote.godtools.android.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.cru.godtools.db.repository.LanguagesRepositoryIT
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class LegacyLanguagesRepositoryIT : LanguagesRepositoryIT() {
    @get:Rule
    internal val dbRule = GodToolsDaoRule()
    override lateinit var repository: LegacyLanguagesRepository

    @Before
    fun setup() {
        repository = LegacyLanguagesRepository(dbRule.dao)
    }
}
