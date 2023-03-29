package org.cru.godtools.user.data

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.account.GodToolsAccountManager
import org.cru.godtools.account.model.AccountInfo
import org.cru.godtools.db.repository.UserRepository
import org.cru.godtools.model.User

private const val USER_ID = "user_id"

@OptIn(ExperimentalCoroutinesApi::class)
class UserManagerTest {
    private val accountInfoFlow = MutableStateFlow<AccountInfo?>(null)
    private val userIdFlow = MutableStateFlow<String?>(null)
    private val userFlow = MutableStateFlow<User?>(null)

    private val accountManager: GodToolsAccountManager = mockk {
        coEvery { accountInfoFlow() } returns accountInfoFlow
        coEvery { userIdFlow() } returns userIdFlow
    }
    private val userRepository: UserRepository = mockk {
        every { findUserFlow(USER_ID) } returns userFlow
    }
    private val testScope = TestScope()

    private val userManager = UserManager(
        accountManager = accountManager,
        userRepository = userRepository,
    )

    @Test
    fun testUserFlow() = testScope.runTest {
        userManager.userFlow.test {
            assertNull(awaitItem(), "userId is null, so there should be no user object")

            val user = User()
            userFlow.value = user
            userIdFlow.value = USER_ID
            assertEquals(user, awaitItem())

            userIdFlow.value = null
            assertNull(awaitItem(), "userId returned to null, so the userFlow should emit no User")
        }
    }
}
