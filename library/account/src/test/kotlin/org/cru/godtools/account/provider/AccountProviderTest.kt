package org.cru.godtools.account.provider

import kotlin.test.Test
import kotlin.test.assertEquals
import okhttp3.ResponseBody.Companion.toResponseBody
import org.ccci.gto.android.common.jsonapi.JsonApiConverter
import org.ccci.gto.android.common.jsonapi.model.JsonApiError
import org.ccci.gto.android.common.jsonapi.model.JsonApiObject
import org.cru.godtools.api.model.AuthToken
import retrofit2.Response

private const val ERROR_USER_ALREADY_EXISTS = """
    {"errors":[{"code":"user_already_exists","detail":"User account already exists."}]}
"""
private const val ERROR_USER_NOT_FOUND = """
    {"errors":[{"code":"user_not_found","detail":"User account not found."}]}
"""

class AccountProviderTest {
    // region extractAuthToken()
    @Test
    fun `extractAuthToken() - Successful`() {
        val token = AuthToken()
        val response = Response.success(JsonApiObject.of(token))

        assertEquals(token, response.extractAuthToken().getOrNull())
    }

    @Test
    fun `extractAuthToken() - Error - Successful status with jsonapi error`() {
        val error = JsonApiError(code = AuthToken.ERROR_USER_ALREADY_EXISTS)
        val response = Response.success(JsonApiObject.error<AuthToken>(error))

        assertEquals(
            AuthenticationException.UserAlreadyExists,
            response.extractAuthToken().exceptionOrNull()
        )
    }

    @Test
    fun `extractAuthToken() - Error - 500 ISE`() {
        val response = Response.error<JsonApiObject<AuthToken>>(500, "500 ISE".toResponseBody())

        assertEquals(
            AuthenticationException.UnknownError,
            response.extractAuthToken().exceptionOrNull()
        )
    }

    @Test
    fun `extractAuthToken() - Error - User Already Exists`() {
        val response = Response.error<JsonApiObject<AuthToken>>(400, ERROR_USER_ALREADY_EXISTS.toResponseBody())

        assertEquals(
            AuthenticationException.UserAlreadyExists,
            response.extractAuthToken().exceptionOrNull()
        )
    }

    @Test
    fun `extractAuthToken() - Error - User Not Found`() {
        val response = Response.error<JsonApiObject<AuthToken>>(400, ERROR_USER_NOT_FOUND.toResponseBody())

        assertEquals(
            AuthenticationException.UserNotFound,
            response.extractAuthToken().exceptionOrNull()
        )
    }

    @Test
    fun `extractAuthToken() - Error - Valid jsonapi response with error status`() {
        val converter = JsonApiConverter.Builder()
            .addClasses(AuthToken::class.java)
            .build()

        val response = Response.error<JsonApiObject<AuthToken>>(
            400,
            converter.toJson(JsonApiObject.of(AuthToken())).toResponseBody()
        )

        assertEquals(
            AuthenticationException.UnknownError,
            response.extractAuthToken().exceptionOrNull()
        )
    }
    // endregion extractAuthToken()
}
