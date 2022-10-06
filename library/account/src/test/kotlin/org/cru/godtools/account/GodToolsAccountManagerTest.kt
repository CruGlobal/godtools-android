package org.cru.godtools.account

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.cru.godtools.account.provider.AccountProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsAccountManagerTest {
    private val provider1Authenticated = MutableStateFlow(false)
    private val provider1 = mockk<AccountProvider>(relaxed = true) {
        every { order } returns 1
        coEvery { isAuthenticated() } answers { provider1Authenticated.value }
        every { isAuthenticatedFlow() } returns provider1Authenticated
    }
    private val provider2Authenticated = MutableStateFlow(false)
    private val provider2 = mockk<AccountProvider>(relaxed = true) {
        every { order } returns 2
        coEvery { isAuthenticated() } answers { provider2Authenticated.value }
        every { isAuthenticatedFlow() } returns provider2Authenticated
    }
    private val manager = GodToolsAccountManager(rawProviders = setOf(provider2, provider1))

    @Test
    fun verifyActiveProvider() = runTest {
        provider1Authenticated.value = true
        assertSame(provider1, manager.activeProvider())
        provider2Authenticated.value = true
        assertSame(provider1, manager.activeProvider())
        provider1Authenticated.value = false
        assertSame(provider2, manager.activeProvider())
        provider2Authenticated.value = false
        assertNull(manager.activeProvider())
    }

    @Test
    fun verifyActiveProviderFlow() = runTest {
        provider1Authenticated.value = true
        manager.activeProviderFlow.test {
            assertSame(provider1, awaitItem())
            assertSame(provider1, manager.activeProviderFlow.value)

            provider2Authenticated.value = true
            expectNoEvents()
            assertSame(provider1, manager.activeProviderFlow.value)

            provider1Authenticated.value = false
            assertSame(provider2, awaitItem())
            assertSame(provider2, manager.activeProviderFlow.value)

            provider2Authenticated.value = false
            assertNull(awaitItem())
            assertNull(manager.activeProviderFlow.value)
        }
    }

    @Test
    fun verifyIsAuthenticated() = runTest {
        provider1Authenticated.value = false
        assertFalse(manager.isAuthenticated())
        provider1Authenticated.value = true
        assertTrue(manager.isAuthenticated())
    }

    @Test
    fun verifyIsAuthenticatedFlow() = runTest {
        provider1Authenticated.value = false
        manager.isAuthenticatedFlow().test {
            assertFalse(awaitItem())

            provider1Authenticated.value = true
            assertTrue(awaitItem())

            provider1Authenticated.value = false
            assertFalse(awaitItem())
        }
    }

    @Test
    fun verifyLogoutTriggersAllProviders() = runTest {
        manager.logout()
        coVerify {
            provider1.logout()
            provider2.logout()
        }
    }
}
