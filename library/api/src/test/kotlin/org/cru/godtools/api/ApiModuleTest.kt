package org.cru.godtools.api

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.cru.godtools.api.model.AuthToken
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import retrofit2.create

private const val TOKEN = "jwt_auth_token"
private const val JSON_RESPONSE_AUTH_TOKEN =
    "{data:{id:\"authtoken\",type:\"auth-token\",attributes:{token:\"$TOKEN\",\"user-id\":1}}}"
private const val JSON_RESPONSE_USER = "{data:{id:\"1\",type:\"user\"}}"

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class ApiModuleTest {
    @get:Rule
    val server = MockWebServer().apply {
        dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest) = when (request.path) {
                "/auth" -> MockResponse().setBody(JSON_RESPONSE_AUTH_TOKEN)
                "/$PATH_USER" -> when (request.getHeader("Authorization")) {
                    TOKEN -> MockResponse().setBody(JSON_RESPONSE_USER)
                    else -> MockResponse().setResponseCode(401)
                }
                else -> MockResponse().setResponseCode(404)
            }
        }
    }

    // only allow a single concurrent call per host to make the shared Dispatcher saturated by the authenticated call
    private val okhttp = OkHttpClient().apply { dispatcher.maxRequestsPerHost = 1 }
    private val retrofit = ApiModule.mobileContentApiRetrofit(
        apiConfig = ApiConfig(mobileContentApiUrl = server.url("/").toString(), cdnUrl = server.url("/").toString()),
        jsonApiConverter = ApiModule.jsonApiConverter(),
        okhttp = okhttp,
    )
    private val authApi = ApiModule.authApi(retrofit, okhttp)
    private val sessionInterceptor = object : MobileContentApiSessionInterceptor(
        ApplicationProvider.getApplicationContext(),
    ) {
        override fun userId() = "1"
        override suspend fun authenticate() = authApi.authenticate(AuthToken.Request()).body()?.dataSingle
    }
    private val userApi = ApiModule.mobileContentApiAuthenticatedRetrofit(retrofit, okhttp, sessionInterceptor)
        .create<UserApi>()

    // region authApi()
    @Test
    fun `authApi() - establishing a session does not wait on the call that needs the session`() = runTest {
        val user = withContext(Dispatchers.Default) { withTimeoutOrNull(10.seconds) { userApi.getUser() } }

        assertNotNull(user, "getUser() deadlocked while establishing a session")
        assertEquals("1", user.body()?.dataSingle?.id)
    }
    // endregion authApi()
}
