package org.cru.godtools.api

import android.content.Context
import android.content.SharedPreferences
import java.net.HttpURLConnection
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import okhttp3.Response
import org.ccci.gto.android.common.api.UserIdSession
import org.ccci.gto.android.common.api.okhttp3.interceptor.SessionInterceptor
import org.cru.godtools.api.model.AuthToken

private const val HTTP_HEADER_AUTHORIZATION = "Authorization"

abstract class MobileContentApiSessionInterceptor(context: Context) : SessionInterceptor<UserIdSession>(context) {
    override fun loadSession(prefs: SharedPreferences) = userId()?.let { UserIdSession(prefs, it) }

    override fun attachSession(request: Request, session: UserIdSession): Request {
        if (!session.isValid) return request

        return request.newBuilder()
            .addHeader(HTTP_HEADER_AUTHORIZATION, session.id!!)
            .build()
    }

    override fun establishSession() = runBlocking {
        authenticate()
            ?.let { UserIdSession(userId = it.userId, sessionId = it.token) }
            ?.takeIf { it.isValid }
    }

    override fun isSessionInvalid(response: Response) = response.code == HttpURLConnection.HTTP_UNAUTHORIZED

    protected abstract fun userId(): String?
    protected abstract suspend fun authenticate(): AuthToken?
}
