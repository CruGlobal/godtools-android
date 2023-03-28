package org.cru.godtools.account.provider.facebook

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.facebook.AccessToken
import com.facebook.AccessTokenManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.facebook.login.currentAccessTokenFlow
import org.junit.runner.RunWith

private const val CLASS_ACCESS_TOKEN_MANAGER_KTX = "org.ccci.gto.android.common.facebook.login.AccessTokenManagerKt"

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FacebookAccountProviderTest {
    private val currentAccessTokenFlow = MutableStateFlow<AccessToken?>(null)

    private val accessTokenManager: AccessTokenManager = mockk {
        every { currentAccessToken } answers { currentAccessTokenFlow.value }
    }
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var provider: FacebookAccountProvider

    @BeforeTest
    fun setup() {
        mockkStatic(CLASS_ACCESS_TOKEN_MANAGER_KTX)
        every { accessTokenManager.currentAccessTokenFlow() } returns currentAccessTokenFlow
        provider = FacebookAccountProvider(
            accessTokenManager = accessTokenManager,
            authApi = mockk(),
            context = context,
            loginManager = mockk()
        )
    }

    @AfterTest
    fun cleanup() {
        unmockkStatic(CLASS_ACCESS_TOKEN_MANAGER_KTX)
    }

    @Test
    fun `userId()`() = runTest {
        assertNull(provider.userId())

        val user = UUID.randomUUID().toString()
        val token = accessToken()
        currentAccessTokenFlow.value = token
        provider.prefs.edit { putString(FacebookAccountProvider.PREF_USER_ID(token), user) }
        assertEquals(user, provider.userId())
    }

    // region userIdFlow()
    @Test
    fun `userIdFlow()`() = runTest {
        val user = UUID.randomUUID().toString()
        val token = accessToken()
        provider.prefs.edit { putString(FacebookAccountProvider.PREF_USER_ID(token), user) }

        provider.userIdFlow().test {
            assertNull(expectMostRecentItem())

            currentAccessTokenFlow.value = token
            runCurrent()
            assertEquals(user, expectMostRecentItem())

            currentAccessTokenFlow.value = null
            runCurrent()
            assertNull(expectMostRecentItem())
        }
    }

    @Test
    fun `userIdFlow() - Emit new userId when it changes`() = runTest {
        val user = UUID.randomUUID().toString()
        val token = accessToken()

        provider.userIdFlow().test {
            currentAccessTokenFlow.value = token
            runCurrent()
            assertNull(expectMostRecentItem())

            provider.prefs.edit { putString(FacebookAccountProvider.PREF_USER_ID(token), user) }
            runCurrent()
            assertEquals(user, expectMostRecentItem())
        }
    }
    // endregion userIdFlow()

    private fun accessToken(userId: String = "user") = AccessToken(
        accessToken = UUID.randomUUID().toString(),
        applicationId = "application",
        userId = userId,
        permissions = null,
        declinedPermissions = null,
        expiredPermissions = null,
        accessTokenSource = null,
        expirationTime = null,
        lastRefreshTime = null,
        dataAccessExpirationTime = null,
    )
}
