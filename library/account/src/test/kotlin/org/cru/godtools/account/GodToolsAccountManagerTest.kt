package org.cru.godtools.account

import android.content.Context
import app.cash.turbine.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.provider.AccountProvider
import org.cru.godtools.api.UserApi
import org.cru.godtools.model.User
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManagerTest {
    private val provider1Authenticated = MutableStateFlow(false)
    private val provider2Authenticated = MutableStateFlow(false)

    private val provider1 = mockk<AccountProvider>(relaxed = true) {
        every { order } returns 1
        coEvery { isAuthenticated } answers { provider1Authenticated.value }
        every { isAuthenticatedFlow() } returns provider1Authenticated

        excludeRecords { isAuthenticatedFlow() }
    }
    private val provider2 = mockk<AccountProvider>(relaxed = true) {
        every { order } returns 2
        coEvery { isAuthenticated } answers { provider2Authenticated.value }
        every { isAuthenticatedFlow() } returns provider2Authenticated

        excludeRecords { isAuthenticatedFlow() }
    }
    private val testScope = TestScope()
    private val userApi: UserApi = mockk()
    private val context: Context = mockk()

    private val manager = GodToolsAccountManager(
        providers = listOf(provider1, provider2),
        userApi = { userApi },
        context = context,
        coroutineScope = testScope.backgroundScope,
    )

    @Test
    fun verifyInjectedProvidersSorted() {
        val manager =
            GodToolsAccountManager(providers = setOf(provider2, provider1), userApi = { userApi }, context = context)
        assertEquals(listOf(provider1, provider2), manager.providers)
    }

    @Test
    fun verifyActiveProvider() = testScope.runTest {
        provider1Authenticated.value = true
        assertSame(provider1, manager.activeProvider)
        provider2Authenticated.value = true
        assertSame(provider1, manager.activeProvider)
        provider1Authenticated.value = false
        assertSame(provider2, manager.activeProvider)
        provider2Authenticated.value = false
        assertNull(manager.activeProvider)
    }

    @Test
    fun verifyActiveProviderFlow() = testScope.runTest {
        manager.activeProviderFlow.test {
            assertNull(awaitItem())

            provider1Authenticated.value = true
            runCurrent()
            assertSame(provider1, awaitItem())
            assertSame(provider1, manager.activeProviderFlow.value)

            provider2Authenticated.value = true
            runCurrent()
            expectNoEvents()
            assertSame(provider1, manager.activeProviderFlow.value)

            provider1Authenticated.value = false
            runCurrent()
            assertSame(provider2, awaitItem())
            assertSame(provider2, manager.activeProviderFlow.value)

            provider2Authenticated.value = false
            runCurrent()
            assertNull(awaitItem())
            assertNull(manager.activeProviderFlow.value)
        }
    }

    @Test
    fun verifyIsAuthenticated() = testScope.runTest {
        provider1Authenticated.value = false
        assertFalse(manager.isAuthenticated)
        provider1Authenticated.value = true
        assertTrue(manager.isAuthenticated)
    }

    @Test
    fun verifyIsAuthenticatedFlow() = testScope.runTest {
        provider1Authenticated.value = false
        manager.isAuthenticatedFlow.test {
            assertFalse(awaitItem())

            provider1Authenticated.value = true
            assertTrue(awaitItem())

            provider1Authenticated.value = false
            assertFalse(awaitItem())
        }
    }

    @Test
    fun verifyLogoutTriggersAllProviders() = testScope.runTest {
        manager.logout()
        coVerify {
            provider1.logout()
            provider2.logout()
        }
    }

    // region deleteAccount()
    @Test
    fun `deleteAccount()`() = testScope.runTest {
        coEvery { userApi.deleteUser() } returns Response.success<JsonApiObject<User>>(204, null)

        assertTrue(manager.deleteAccount())
        coVerifySequence {
            userApi.deleteUser()
            provider1.logout()
            provider2.logout()
        }
    }

    @Test
    fun `deleteAccount() - not authenticated`() = testScope.runTest {
        coEvery { userApi.deleteUser() } returns Response.error(401, "".toResponseBody())

        assertFalse(manager.deleteAccount())
        coVerifySequence {
            userApi.deleteUser()

            // deleting the user failed, so don't log the user out
            provider1 wasNot Called
            provider2 wasNot Called
        }
    }

    @Test
    fun `deleteAccount() - IOException`() = testScope.runTest {
        coEvery { userApi.deleteUser() } throws IOException()

        assertFalse(manager.deleteAccount())
        coVerifySequence {
            userApi.deleteUser()

            // deleting the user failed, so don't log the user out
            provider1 wasNot Called
            provider2 wasNot Called
        }
    }
    // endregion deleteAccount()
}
