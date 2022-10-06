package org.cru.godtools.account.provider.okta

import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.credential.Credential
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundationbootstrap.CredentialBootstrap
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.model.JsonApiError
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.okta.authfoundation.credential.ChangeAwareTokenStorage
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import retrofit2.Response

private const val ACCESS_TOKEN = "access_token"
private const val USER_ID = "user_id"

@OptIn(ExperimentalCoroutinesApi::class)
class OktaAccountProviderTest {
    private val api = mockk<AuthApi>()
    private val credential = mockk<Credential> {
        every { tags } returns emptyMap()
        every { token } returns mockk()

        excludeRecords {
            tags
            token
        }
    }
    private lateinit var provider: OktaAccountProvider

    @Before
    fun setupMocks() {
        val storage = mockk<ChangeAwareTokenStorage>()
        val credentials = mockk<CredentialBootstrap> {
            coEvery { defaultCredential() } returns credential
            every { credentialDataSource } returns mockk<OidcClient>().createCredentialDataSource(storage)
        }
        provider = OktaAccountProvider(credentials, api)
    }

    // region authenticateWithMobileContentApi()
    @Test
    fun `authenticateWithMobileContentApi()`() = runTest {
        val token = AuthToken().apply { userId = USER_ID }
        val tags = slot<Map<String, String>>()
        coEvery { credential.getValidAccessToken() } returns ACCESS_TOKEN
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of(token))
        coEvery { credential.storeToken(tags = capture(tags)) } just Runs

        assertSame(token, provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api.authenticate(match { it.oktaAccessToken == ACCESS_TOKEN })
            credential.storeToken(any(), mapOf(OktaAccountProvider.TAG_USER_ID to USER_ID))
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - No Valid AccessToken`() = runTest {
        coEvery { credential.getValidAccessToken() } returns null

        assertNull(provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api wasNot Called
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - HTTP 401`() = runTest {
        coEvery { credential.getValidAccessToken() } returns ACCESS_TOKEN
        coEvery { api.authenticate(any()) } returns Response.error(401, "".toResponseBody())

        assertNull(provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api.authenticate(match { it.oktaAccessToken == ACCESS_TOKEN })
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - jsonapi errors`() = runTest {
        coEvery { credential.getValidAccessToken() } returns ACCESS_TOKEN
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.error(JsonApiError()))

        assertNull(provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api.authenticate(match { it.oktaAccessToken == ACCESS_TOKEN })
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - missing token`() = runTest {
        coEvery { credential.getValidAccessToken() } returns ACCESS_TOKEN
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of())

        assertNull(provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api.authenticate(match { it.oktaAccessToken == ACCESS_TOKEN })
        }
    }

    @Test
    fun `authenticateWithMobileContentApi() - Error - token without userId`() = runTest {
        val token = AuthToken()
        coEvery { credential.getValidAccessToken() } returns ACCESS_TOKEN
        coEvery { api.authenticate(any()) } returns Response.success(JsonApiObject.of(token))

        assertSame(token, provider.authenticateWithMobileContentApi())
        coVerifyAll {
            credential.getValidAccessToken()
            api.authenticate(match { it.oktaAccessToken == ACCESS_TOKEN })
        }
    }
    // endregion authenticateWithMobileContentApi()
}
