package org.cru.godtools.account.provider.google

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Tasks
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkObject
import io.mockk.unmockkStatic
import java.util.UUID
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.play.auth.signin.GoogleSignInKtx
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.account.provider.google.GoogleAccountProvider.Companion.PREF_USER_ID
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import org.junit.runner.RunWith
import retrofit2.Response

private const val ID_TOKEN_INVALID = "invalid"
private const val ID_TOKEN_VALID = "valid"

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class GoogleAccountProviderTest {
    private val lastSignedInAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    private val userId = UUID.randomUUID().toString()

    private val authApi: AuthApi = mockk()
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val googleSignInClient: GoogleSignInClient = mockk()

    private lateinit var provider: GoogleAccountProvider

    @BeforeTest
    fun setup() {
        mockkObject(GoogleSignInKtx)
        every { GoogleSignInKtx.getLastSignedInAccountFlow(any()) } returns lastSignedInAccount
        mockkStatic(GoogleSignIn::class)
        every { GoogleSignIn.getLastSignedInAccount(any()) } answers { lastSignedInAccount.value }
        provider = GoogleAccountProvider(
            authApi = authApi,
            context = context,
            googleSignInClient = googleSignInClient,
        )
    }

    @AfterTest
    fun cleanup() {
        unmockkStatic(GoogleSignIn::class)
        unmockkObject(GoogleSignInKtx)
    }

    // region Property: isAuthenticated
    @Test
    fun `Property isAuthenticated`() = runTest {
        assertFalse(provider.isAuthenticated, "No GoogleSignInAccount")

        val account = GoogleSignInAccount.createDefault()
        lastSignedInAccount.value = account
        assertFalse(provider.isAuthenticated, "GoogleSignInAccount but no userId")

        provider.prefs.edit { putString(account.PREF_USER_ID, userId) }
        assertTrue(provider.isAuthenticated, "GoogleSignInAccount w/ userId")

        lastSignedInAccount.value = null
        assertFalse(provider.isAuthenticated, "No GoogleSignInAccount, still has userId")
    }
    // endregion Property: isAuthenticated

    // region isAuthenticatedFlow()
    @Test
    fun `isAuthenticatedFlow()`() = runTest {
        val account = GoogleSignInAccount.createDefault()
        provider.prefs.edit { putString(account.PREF_USER_ID, userId) }

        provider.isAuthenticatedFlow().test {
            assertFalse(awaitItem())

            lastSignedInAccount.value = account
            assertTrue(awaitItem())

            provider.prefs.edit { clear() }
            assertFalse(awaitItem())

            provider.prefs.edit { putString(account.PREF_USER_ID, userId) }
            assertTrue(awaitItem())

            lastSignedInAccount.value = null
            assertFalse(awaitItem())
        }
    }
    // endregion isAuthenticatedFlow()

    // region userIdFlow()
    @Test
    fun `userIdFlow()`() = runTest {
        val account = GoogleSignInAccount.createDefault()
        provider.prefs.edit { putString(account.PREF_USER_ID, userId) }

        provider.userIdFlow().test {
            assertNull(awaitItem())

            lastSignedInAccount.value = account
            assertEquals(userId, awaitItem())

            lastSignedInAccount.value = null
            assertNull(awaitItem())
        }
    }

    @Test
    fun `userIdFlow() - emits when account updates userId`() = runTest {
        val account = GoogleSignInAccount.createDefault()

        provider.userIdFlow().test {
            runCurrent()
            assertNull(expectMostRecentItem())

            lastSignedInAccount.value = account
            runCurrent()

            provider.prefs.edit { putString(account.PREF_USER_ID, userId) }
            runCurrent()
            assertEquals(userId, expectMostRecentItem())

            val userId2 = UUID.randomUUID().toString()
            provider.prefs.edit { putString(account.PREF_USER_ID, userId2) }
            runCurrent()
            assertEquals(userId2, expectMostRecentItem())
        }
    }
    // endregion userIdFlow()

    // region authenticateWithMobileContentApi()
    private val authToken = AuthToken(userId, "token")
    private val createUser = Random.nextBoolean()
    private val validAccount: GoogleSignInAccount = mockk {
        every { id } returns UUID.randomUUID().toString()
        every { idToken } returns ID_TOKEN_VALID
    }

    @BeforeTest
    fun `Setup authenticateWithMobileContentApi()`() {
        every { googleSignInClient.silentSignIn() } returns Tasks.forResult(validAccount)

        coEvery { authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser)) }
            .returns(Response.success(JsonApiObject.single(authToken)))
        coEvery { authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser)) }
            .returns(Response.error(401, "".toResponseBody()))
    }

    @Test
    fun `authenticateWithMobileContentApi()`() = runTest {
        lastSignedInAccount.value = validAccount

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifySequence {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))

            googleSignInClient wasNot Called
        }
        assertEquals(
            userId,
            provider.prefs.getString(lastSignedInAccount.value!!.PREF_USER_ID, "")
        )
    }

    @Test
    fun `authenticateWithMobileContentApi() - Not authenticated`() = runTest {
        lastSignedInAccount.value = null

        assertEquals(
            Result.failure(AuthenticationException.MissingCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            authApi wasNot Called
            googleSignInClient wasNot Called
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - No id_token`() = runTest {
        lastSignedInAccount.value = mockk { every { idToken } returns null }

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifySequence {
            googleSignInClient.silentSignIn()
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - invalid id_token`() = runTest {
        lastSignedInAccount.value = mockk { every { idToken } returns ID_TOKEN_INVALID }

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifySequence {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser))
            googleSignInClient.silentSignIn()
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))
        }
    }
    // endregion authenticateWithMobileContentApi()
}
