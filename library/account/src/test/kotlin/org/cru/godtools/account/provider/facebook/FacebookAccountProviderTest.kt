package org.cru.godtools.account.provider.facebook

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.facebook.AccessToken
import com.facebook.AccessTokenManager
import com.facebook.FacebookException
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.UUID
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.facebook.login.currentAccessTokenFlow
import org.ccci.gto.android.common.facebook.login.refreshCurrentAccessToken
import org.ccci.gto.android.common.jsonapi.model.JsonApiError
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.account.provider.facebook.FacebookAccountProvider.Companion.PREF_USER_ID
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import org.junit.runner.RunWith
import retrofit2.Response

private const val CLASS_ACCESS_TOKEN_MANAGER_KTX = "org.ccci.gto.android.common.facebook.login.AccessTokenManagerKt"

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class FacebookAccountProviderTest {
    private val currentAccessTokenFlow = MutableStateFlow<AccessToken?>(null)

    private val accessTokenManager: AccessTokenManager = mockk {
        every { currentAccessToken } answers { currentAccessTokenFlow.value }
    }
    private val api: AuthApi = mockk()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private lateinit var provider: FacebookAccountProvider

    @BeforeTest
    fun setup() {
        mockkStatic(CLASS_ACCESS_TOKEN_MANAGER_KTX)
        every { accessTokenManager.currentAccessTokenFlow() } returns currentAccessTokenFlow
        coEvery { accessTokenManager.refreshCurrentAccessToken() } returns null
        provider = FacebookAccountProvider(
            accessTokenManager = accessTokenManager,
            authApi = api,
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
        assertNull(provider.userId)

        val user = UUID.randomUUID().toString()
        val token = accessToken()
        currentAccessTokenFlow.value = token
        provider.prefs.edit { putString(token.PREF_USER_ID, user) }
        assertEquals(user, provider.userId)
    }

    // region userIdFlow()
    @Test
    fun `userIdFlow()`() = runTest {
        val user = UUID.randomUUID().toString()
        val token = accessToken()
        provider.prefs.edit { putString(token.PREF_USER_ID, user) }

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

            provider.prefs.edit { putString(token.PREF_USER_ID, user) }
            runCurrent()
            assertEquals(user, expectMostRecentItem())
        }
    }
    // endregion userIdFlow()

    // region authenticateWithMobileContentApi()
    @Test
    fun `authenticateWithMobileContentApi()`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        val token = AuthToken(userId = UUID.randomUUID().toString())
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of(token))

        assertEquals(Result.success(token), provider.authenticateWithMobileContentApi(createUser))
        assertEquals(token.userId, provider.userId)
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - No Valid AccessToken`() = runTest {
        assertEquals(
            Result.failure(AuthenticationException.MissingCredentials),
            provider.authenticateWithMobileContentApi(true)
        )

        coVerifyAll {
            api wasNot Called
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - Refresh successful`() = runTest {
        val accessToken = accessToken()
        val accessToken2 = accessToken()
        val createUser = Random.nextBoolean()
        val token = AuthToken(userId = UUID.randomUUID().toString())
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser)) }
            .returns(Response.error(401, "".toResponseBody()))
        coEvery { accessTokenManager.refreshCurrentAccessToken() } returns accessToken2
        coEvery { api.authenticate(AuthToken.Request(fbAccessToken = accessToken2.token, createUser = createUser)) }
            .returns(Response.success(JsonApiObject.of(token)))

        assertEquals(Result.success(token), provider.authenticateWithMobileContentApi(createUser))
        assertEquals(token.userId, provider.userId)
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
            accessTokenManager.refreshCurrentAccessToken()
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken2.token, createUser = createUser))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - Refresh doesn't return access_token`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.error(401, "".toResponseBody())
        coEvery { accessTokenManager.refreshCurrentAccessToken() } returns null

        assertEquals(
            Result.failure(AuthenticationException.UnableToRefreshCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
            accessTokenManager.refreshCurrentAccessToken()
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - Refresh throws exception`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.error(401, "".toResponseBody())
        coEvery { accessTokenManager.refreshCurrentAccessToken() } throws FacebookException()

        assertEquals(
            Result.failure(AuthenticationException.UnableToRefreshCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
            accessTokenManager.refreshCurrentAccessToken()
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - jsonapi errors`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.error(JsonApiError()))

        assertEquals(
            Result.failure(AuthenticationException.UnknownError),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - missing response auth_token`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of())

        assertEquals(
            Result.failure(AuthenticationException.UnknownError),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - auth_token without userId`() = runTest {
        val accessToken = accessToken()
        val createUser = Random.nextBoolean()
        val token = AuthToken()
        currentAccessTokenFlow.value = accessToken
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of(token))

        assertEquals(Result.success(token), provider.authenticateWithMobileContentApi(createUser))
        assertNull(provider.userId)
        coVerifyAll {
            api.authenticate(AuthToken.Request(fbAccessToken = accessToken.token, createUser = createUser))
        }
    }
    // endregion authenticateWithMobileContentApi()

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
