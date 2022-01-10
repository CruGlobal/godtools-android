package org.cru.godtools.api

import android.content.Context
import android.content.SharedPreferences
import com.okta.oidc.Tokens
import com.okta.oidc.clients.sessions.SessionClient
import com.okta.oidc.util.AuthorizationException
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.ccci.gto.android.common.api.UserIdSession
import org.ccci.gto.android.common.api.okhttp3.interceptor.SessionInterceptor
import org.ccci.gto.android.common.okta.oidc.clients.sessions.oktaUserId
import org.ccci.gto.android.common.okta.oidc.clients.sessions.refreshToken
import org.ccci.gto.android.common.okta.oidc.oktaUserId
import org.cru.godtools.api.model.AuthToken

private const val HTTP_HEADER_AUTHORIZATION = "Authorization"

@Singleton
class GodToolsSessionInterceptor @Inject constructor(
    @ApplicationContext context: Context,
    private val authApi: AuthApi,
    private val sessionClient: SessionClient
) : SessionInterceptor<UserIdSession>(context = context) {
    override fun loadSession(prefs: SharedPreferences) = sessionClient.oktaUserId?.let { UserIdSession(prefs, it) }

    override fun attachSession(request: Request, session: UserIdSession): Request {
        if (!session.isValid) return request

        return request.newBuilder()
            .addHeader(HTTP_HEADER_AUTHORIZATION, session.id!!)
            .build()
    }

    override fun establishSession() = sessionClient.tokens?.let { authenticateWithOktaTokens(it) }
        ?: runBlocking {
            try {
                sessionClient.refreshToken()
            } catch (e: AuthorizationException) {
                null
            }
        }?.let { authenticateWithOktaTokens(it) }

    private fun authenticateWithOktaTokens(tokens: Tokens): UserIdSession? {
        val request = tokens.accessToken?.let { AuthToken.Request(it) } ?: return null
        return authApi.authenticate(request).execute().takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.dataSingle?.let { UserIdSession(userId = tokens.oktaUserId, sessionId = it.token) }
            ?.takeIf { it.isValid }
    }

    override fun isSessionInvalid(response: Response) = response.code == HttpURLConnection.HTTP_UNAUTHORIZED
}
