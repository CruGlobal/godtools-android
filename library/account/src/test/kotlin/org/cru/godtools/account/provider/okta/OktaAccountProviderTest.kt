package org.cru.godtools.account.provider.okta

import app.cash.turbine.test
import com.okta.authfoundation.claims.email
import com.okta.authfoundation.claims.familyName
import com.okta.authfoundation.claims.givenName
import com.okta.authfoundation.claims.name
import com.okta.authfoundation.claims.subject
import com.okta.authfoundation.client.OidcClient
import com.okta.authfoundation.client.dto.OidcUserInfo
import com.okta.authfoundation.credential.Credential
import com.okta.authfoundation.credential.CredentialDataSource.Companion.createCredentialDataSource
import com.okta.authfoundation.jwt.Jwt
import com.okta.authfoundationbootstrap.CredentialBootstrap
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.model.JsonApiError
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.ccci.gto.android.common.okta.authfoundation.client.dto.grMasterPersonId
import org.ccci.gto.android.common.okta.authfoundation.client.dto.oktaUserId
import org.ccci.gto.android.common.okta.authfoundation.client.dto.ssoGuid
import org.ccci.gto.android.common.okta.authfoundation.credential.ChangeAwareTokenStorage
import org.ccci.gto.android.common.okta.authfoundation.credential.idTokenFlow
import org.ccci.gto.android.common.okta.authfoundation.credential.userInfoFlow
import org.cru.godtools.api.AuthApi
import org.cru.godtools.api.model.AuthToken
import retrofit2.Response

private const val ACCESS_TOKEN = "access_token"
private const val USER_ID = "user_id"
private const val OKTA_ID = "okta_id"
private const val SSO_GUID = "ssoGuid"
private const val GR_MASTER_PERSON_ID = "gr_id"
private const val NAME = "name"
private const val EMAIL = "email"

@OptIn(ExperimentalCoroutinesApi::class)
class OktaAccountProviderTest {
    private val api = mockk<AuthApi>()
    private val idTokenFlow = MutableStateFlow<Jwt?>(null)
    private val userInfoFlow = MutableStateFlow<OidcUserInfo?>(null)
    private val credential = mockk<Credential> {
        every { tags } returns mapOf(OktaAccountProvider.TAG_USER_ID to USER_ID)
        every { token } returns mockk()

        mockkStatic("org.ccci.gto.android.common.okta.authfoundation.credential.Credential_FlowKt")
        every { idTokenFlow() } returns idTokenFlow
        every { userInfoFlow() } returns userInfoFlow

        excludeRecords {
            tags
            token
        }
    }
    private lateinit var provider: OktaAccountProvider

    @BeforeTest
    fun setupMocks() {
        val storage = mockk<ChangeAwareTokenStorage>(relaxed = true)
        val credentials = mockk<CredentialBootstrap> {
            coEvery { defaultCredential() } returns credential
            every { credentialDataSource } returns mockk<OidcClient>().createCredentialDataSource(storage)
        }
        provider = OktaAccountProvider(credentials, api)
    }

    // region logout()
    @Test
    fun `logout()`() = runTest {
        coEvery { credential.revokeAllTokens() } returns mockk()
        coEvery { credential.delete() } just Runs

        provider.logout()
        coVerifyOrder {
            credential.revokeAllTokens()
            credential.delete()
        }
    }
    // endregion logout()

    // region accountInfoFlow()
    private fun idToken(
        subject: String? = OKTA_ID,
        name: String? = NAME,
        email: String? = EMAIL,
    ) = mockk<Jwt> {
        every { this@mockk.subject } returns subject
        every { this@mockk.name } returns name
        every { this@mockk.email } returns email
    }

    private fun userInfo(
        oktaUserId: String? = OKTA_ID,
        ssoGuid: String? = SSO_GUID,
        grMasterPersonId: String? = GR_MASTER_PERSON_ID,
        email: String? = EMAIL,
    ) = mockk<OidcUserInfo> {
        every { this@mockk.oktaUserId } returns oktaUserId
        every { this@mockk.ssoGuid } returns ssoGuid
        every { this@mockk.grMasterPersonId } returns grMasterPersonId
        every { this@mockk.email } returns email
        every { givenName } returns null
        every { familyName } returns null
    }

    @Test
    fun `accountInfoFlow() - no idToken or userInfo`() = runTest {
        idTokenFlow.value = null
        userInfoFlow.value = null
        provider.accountInfoFlow().test {
            assertNull(awaitItem(), "accountInfoFlow() should emit null if there is no idToken or userInfo")
        }
    }

    @Test
    fun `accountInfoFlow() - idToken only`() = runTest {
        idTokenFlow.value = idToken()
        userInfoFlow.value = null
        provider.accountInfoFlow().test {
            val info = assertNotNull(awaitItem())
            assertEquals(OKTA_ID, info.oktaUserId)
            assertEquals(NAME, info.name)
            assertEquals(EMAIL, info.email)
            assertNull(info.ssoGuid)
            assertNull(info.grMasterPersonId)
        }
    }

    @Test
    fun `accountInfoFlow() - userInfo only`() = runTest {
        idTokenFlow.value = null
        userInfoFlow.value = userInfo()
        provider.accountInfoFlow().test {
            val info = assertNotNull(awaitItem())
            assertEquals(OKTA_ID, info.oktaUserId)
            assertEquals(SSO_GUID, info.ssoGuid)
            assertEquals(GR_MASTER_PERSON_ID, info.grMasterPersonId)
            assertNull(info.name)
            assertEquals(EMAIL, info.email)
        }
    }

    @Test
    fun `accountInfoFlow() - idToken & userInfo`() = runTest {
        idTokenFlow.value = idToken()
        userInfoFlow.value = userInfo()
        provider.accountInfoFlow().test {
            assertNotNull(awaitItem()) { info ->
                assertEquals(OKTA_ID, info.oktaUserId)
                assertEquals(SSO_GUID, info.ssoGuid)
                assertEquals(GR_MASTER_PERSON_ID, info.grMasterPersonId)
                assertEquals(NAME, info.name)
                assertEquals(EMAIL, info.email)
            }

            // we prefer attributes from idToken when we have both data sources
            idTokenFlow.value = idToken(subject = "id_$OKTA_ID", email = "id_$EMAIL")
            assertNotNull(awaitItem()) { info ->
                assertEquals("id_$OKTA_ID", info.oktaUserId)
                assertEquals(SSO_GUID, info.ssoGuid)
                assertEquals(GR_MASTER_PERSON_ID, info.grMasterPersonId)
                assertEquals(NAME, info.name)
                assertEquals("id_$EMAIL", info.email)
            }
        }
    }
    // endregion accountInfoFlow()

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
