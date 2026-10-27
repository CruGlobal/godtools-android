package org.cru.godtools.account.provider.google

import android.content.Context
import android.os.Bundle
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialUnknownException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.net.UnknownHostException
import java.util.UUID
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.account.provider.AuthenticationException
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import org.junit.runner.RunWith
import retrofit2.Response

private const val ACCOUNT_ID = "account@example.com"
private const val ID_TOKEN_VALID = "valid"
private const val ID_TOKEN_INVALID = "invalid"
private const val ID_TOKEN_REFRESHED = "refreshed"
private const val SERVER_CLIENT_ID = "server_client_id"

private const val TEST_EXTRA_ID = "test_id"
private const val TEST_EXTRA_ID_TOKEN = "test_id_token"

@RunWith(AndroidJUnit4::class)
class GoogleAccountProviderTest {
    private val userId = UUID.randomUUID().toString()
    private val authToken = AuthToken(userId, "token")
    private val createUser = Random.nextBoolean()

    private val authApi: AuthApi = mockk {
        coEvery { authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser)) }
            .returns(Response.success(JsonApiObject.single(authToken)))
        coEvery { authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_REFRESHED, createUser = createUser)) }
            .returns(Response.success(JsonApiObject.single(authToken)))
        coEvery { authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser)) }
            .returns(Response.error(401, "".toResponseBody()))
    }
    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val credentialManager: CredentialManager = mockk {
        coEvery { getCredential(any(), any<GetCredentialRequest>()) } throws NoCredentialException()
        coEvery { clearCredentialState(any()) } just Runs
    }
    private val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(SERVER_CLIENT_ID).build()
    private val authorizedGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(true)
        .setServerClientId(SERVER_CLIENT_ID)
        .build()

    private lateinit var provider: GoogleAccountProvider

    @BeforeTest
    fun setup() {
        mockkObject(GoogleIdTokenCredential)
        every { GoogleIdTokenCredential.createFrom(any()) } answers {
            val data = firstArg<Bundle>()
            mockk {
                every { id } returns data.getString(TEST_EXTRA_ID)!!
                every { idToken } returns data.getString(TEST_EXTRA_ID_TOKEN)!!
            }
        }
        provider = createProvider()
    }

    @AfterTest
    fun cleanup() {
        unmockkObject(GoogleIdTokenCredential)
    }

    private fun createProvider() = GoogleAccountProvider(
        authApi = authApi,
        context = context,
        credentialManager = credentialManager,
        signInWithGoogleOption = signInWithGoogleOption,
        authorizedGoogleIdOption = authorizedGoogleIdOption,
    )

    private fun googleCredentialResponse(id: String = ACCOUNT_ID, idToken: String = ID_TOKEN_VALID) =
        GetCredentialResponse(
            CustomCredential(
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
                bundleOf(TEST_EXTRA_ID to id, TEST_EXTRA_ID_TOKEN to idToken),
            )
        )

    private fun storeAccount(idToken: String? = ID_TOKEN_VALID, userId: String? = null) = provider.prefs.edit {
        putString(PREF_ACCOUNT_ID, ACCOUNT_ID)
        putString(PREF_ID_TOKEN, idToken)
        putString(PREF_USER_ID, userId)
    }

    // region Property: isAuthenticated
    @Test
    fun `Property isAuthenticated`() {
        assertFalse(provider.isAuthenticated, "No stored account")

        storeAccount()
        assertFalse(provider.isAuthenticated, "Stored account but no userId")

        storeAccount(userId = userId)
        assertTrue(provider.isAuthenticated, "Stored account w/ userId")

        provider.prefs.edit { clear() }
        assertFalse(provider.isAuthenticated, "Cleared account")
    }
    // endregion Property: isAuthenticated

    // region Property userId
    @Test
    fun `Property userId - persisted across provider instances`() {
        storeAccount(userId = userId)

        val recreated = createProvider()
        assertTrue(recreated.isAuthenticated)
        assertEquals(userId, recreated.userId)
    }
    // endregion Property userId

    // region isAuthenticatedFlow()
    @Test
    fun `isAuthenticatedFlow()`() = runTest {
        provider.isAuthenticatedFlow().test {
            assertFalse(awaitItem())

            storeAccount(userId = userId)
            assertTrue(awaitItem())

            provider.prefs.edit { clear() }
            assertFalse(awaitItem())
        }
    }
    // endregion isAuthenticatedFlow()

    // region userIdFlow()
    @Test
    fun `userIdFlow()`() = runTest {
        provider.userIdFlow().test {
            assertNull(awaitItem())

            storeAccount(userId = userId)
            assertEquals(userId, awaitItem())

            val userId2 = UUID.randomUUID().toString()
            provider.prefs.edit { putString(PREF_USER_ID, userId2) }
            assertEquals(userId2, awaitItem())

            provider.prefs.edit { clear() }
            assertNull(awaitItem())
        }
    }
    // endregion userIdFlow()

    // region login()
    @Test
    fun `login()`() = runTest {
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse())

        assertEquals(Result.success(authToken), provider.login(context, createUser))
        coVerifySequence {
            credentialManager.getCredential(
                context,
                match<GetCredentialRequest> { it.credentialOptions == listOf(signInWithGoogleOption) }
            )
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))
        }
        assertEquals(ACCOUNT_ID, provider.prefs.getString(PREF_ACCOUNT_ID, null))
        assertEquals(ID_TOKEN_VALID, provider.prefs.getString(PREF_ID_TOKEN, null))
        assertEquals(userId, provider.userId)
    }

    @Test
    fun `login() - Cancelled`() = runTest {
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .throws(GetCredentialCancellationException())

        assertEquals(Result.failure(AuthenticationException.MissingCredentials), provider.login(context, createUser))
        coVerifyAll { authApi wasNot Called }
        assertNull(provider.prefs.getString(PREF_ACCOUNT_ID, null))
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `login() - No credentials`() = runTest {
        assertEquals(Result.failure(AuthenticationException.MissingCredentials), provider.login(context, createUser))
        coVerifyAll { authApi wasNot Called }
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `login() - Unexpected credential type`() = runTest {
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(GetCredentialResponse(CustomCredential("unexpected", Bundle())))

        assertEquals(Result.failure(AuthenticationException.MissingCredentials), provider.login(context, createUser))
        coVerifyAll { authApi wasNot Called }
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `login() - Parsing failure`() = runTest {
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse())
        every { GoogleIdTokenCredential.createFrom(any()) } throws mockk<GoogleIdTokenParsingException>()

        assertEquals(Result.failure(AuthenticationException.MissingCredentials), provider.login(context, createUser))
        coVerifyAll { authApi wasNot Called }
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `login() - Api Exception - UnknownHostException()`() = runTest {
        val exception = UnknownHostException()
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse())
        coEvery { authApi.authenticate(any()) } throws exception

        assertEquals(Result.failure(exception), provider.login(context, createUser))
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `login() - Api rejects id_token`() = runTest {
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse(idToken = ID_TOKEN_INVALID))

        assertEquals(Result.failure(AuthenticationException.UnknownError), provider.login(context, createUser))
        coVerify(exactly = 1) { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
        assertFalse(provider.isAuthenticated)
    }
    // endregion login()

    // region logout()
    @Test
    fun `logout()`() = runTest {
        storeAccount(userId = userId)

        provider.logout()
        coVerifyAll { credentialManager.clearCredentialState(any()) }
        assertNull(provider.prefs.getString(PREF_ACCOUNT_ID, null))
        assertNull(provider.prefs.getString(PREF_ID_TOKEN, null))
        assertFalse(provider.isAuthenticated)
    }

    @Test
    fun `logout() - clearCredentialState() fails`() = runTest {
        coEvery { credentialManager.clearCredentialState(any()) } throws ClearCredentialUnknownException()
        storeAccount(userId = userId)

        provider.logout()
        assertNull(provider.prefs.getString(PREF_ACCOUNT_ID, null))
        assertNull(provider.prefs.getString(PREF_ID_TOKEN, null))
        assertFalse(provider.isAuthenticated)
    }
    // endregion logout()

    // region authenticateWithMobileContentApi()
    @Test
    fun `authenticateWithMobileContentApi()`() = runTest {
        storeAccount()

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifyAll {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))
            credentialManager wasNot Called
        }
        assertEquals(userId, provider.userId)
    }

    @Test
    fun `authenticateWithMobileContentApi() - Api Exception - UnknownHostException()`() = runTest {
        val exception = UnknownHostException()
        storeAccount()
        coEvery { authApi.authenticate(any()) } throws exception

        assertEquals(Result.failure(exception), provider.authenticateWithMobileContentApi(createUser))
        coVerifyAll {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_VALID, createUser = createUser))
            credentialManager wasNot Called
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Not authenticated`() = runTest {
        assertEquals(
            Result.failure(AuthenticationException.MissingCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifyAll {
            authApi wasNot Called
            credentialManager wasNot Called
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - No id_token`() = runTest {
        storeAccount(idToken = null)
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse(idToken = ID_TOKEN_REFRESHED))

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifySequence {
            credentialManager.getCredential(
                context,
                match<GetCredentialRequest> { it.credentialOptions == listOf(authorizedGoogleIdOption) }
            )
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_REFRESHED, createUser = createUser))
        }
        assertEquals(ID_TOKEN_REFRESHED, provider.prefs.getString(PREF_ID_TOKEN, null))
        assertEquals(userId, provider.userId)
    }

    @Test
    fun `authenticateWithMobileContentApi() - invalid id_token - refreshes credential`() = runTest {
        storeAccount(idToken = ID_TOKEN_INVALID)
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse(idToken = ID_TOKEN_REFRESHED))

        assertEquals(Result.success(authToken), provider.authenticateWithMobileContentApi(createUser))
        coVerifySequence {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser))
            credentialManager.getCredential(
                context,
                match<GetCredentialRequest> { it.credentialOptions == listOf(authorizedGoogleIdOption) }
            )
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_REFRESHED, createUser = createUser))
        }
        assertEquals(ID_TOKEN_REFRESHED, provider.prefs.getString(PREF_ID_TOKEN, null))
    }

    @Test
    fun `authenticateWithMobileContentApi() - invalid id_token - refresh fails`() = runTest {
        storeAccount(idToken = ID_TOKEN_INVALID)

        assertEquals(
            Result.failure(AuthenticationException.UnableToRefreshCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifySequence {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser))
            credentialManager.getCredential(any(), any<GetCredentialRequest>())
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - invalid id_token - refresh returns different account`() = runTest {
        storeAccount(idToken = ID_TOKEN_INVALID)
        coEvery { credentialManager.getCredential(any(), any<GetCredentialRequest>()) }
            .returns(googleCredentialResponse(id = "other@example.com", idToken = ID_TOKEN_REFRESHED))

        assertEquals(
            Result.failure(AuthenticationException.UnableToRefreshCredentials),
            provider.authenticateWithMobileContentApi(createUser)
        )
        coVerifySequence {
            authApi.authenticate(AuthToken.Request(googleIdToken = ID_TOKEN_INVALID, createUser = createUser))
            credentialManager.getCredential(any(), any<GetCredentialRequest>())
        }
        assertEquals(ID_TOKEN_INVALID, provider.prefs.getString(PREF_ID_TOKEN, null))
    }
    // endregion authenticateWithMobileContentApi()
}
