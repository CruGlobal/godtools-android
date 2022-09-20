package org.cru.godtools.api

import android.content.Context
import android.content.SharedPreferences
import com.okta.authfoundationbootstrap.CredentialBootstrap
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.HttpURLConnection
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.ccci.gto.android.common.api.UserIdSession
import org.ccci.gto.android.common.api.okhttp3.interceptor.SessionInterceptor
import org.ccci.gto.android.common.okta.authfoundation.credential.getOktaUserId
import org.cru.godtools.api.model.AuthToken

private const val HTTP_HEADER_AUTHORIZATION = "Authorization"

@Singleton
class GodToolsSessionInterceptor @Inject constructor(
    @ApplicationContext context: Context,
    private val authApi: AuthApi,
    private val credentials: CredentialBootstrap
) : SessionInterceptor<UserIdSession>(context = context) {
    override fun loadSession(prefs: SharedPreferences) =
        runBlocking { credentials.defaultCredential().getOktaUserId()?.let { UserIdSession(prefs, it) } }

    override fun attachSession(request: Request, session: UserIdSession): Request {
        if (!session.isValid) return request

        return request.newBuilder()
            .addHeader(HTTP_HEADER_AUTHORIZATION, session.id!!)
            .build()
    }

    override fun establishSession() = runBlocking {
        val credential = credentials.defaultCredential()
        val request = credential.getValidAccessToken()?.let { AuthToken.Request(it) } ?: return@runBlocking null
        authApi.authenticate(request).execute().takeIf { it.isSuccessful }
            ?.body()?.takeUnless { it.hasErrors() }
            ?.dataSingle?.let { UserIdSession(userId = credential.getOktaUserId(), sessionId = it.token) }
            ?.takeIf { it.isValid }
    }

    override fun isSessionInvalid(response: Response) = response.code == HttpURLConnection.HTTP_UNAUTHORIZED
}
