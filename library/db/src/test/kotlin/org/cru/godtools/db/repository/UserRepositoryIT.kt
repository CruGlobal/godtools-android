package org.cru.godtools.db.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.randomUser

abstract class UserRepositoryIT {
    internal abstract val repository: UserRepository

    @Test
    fun `findUser()`() = runTest {
        val user = randomUser()
        repository.storeUserFromSync(user)

        assertEquals(user, repository.findUser(user.id))
    }

    @Test
    fun `findUser() - Doesn't Exist`() = runTest {
        assertNull(repository.findUser("invalid"))
    }
}
