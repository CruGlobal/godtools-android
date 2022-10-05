package org.cru.godtools.account.provider

import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.api.model.AuthToken

internal interface AccountProvider : Ordered {
    val type: Type

    suspend fun isAuthenticated(): Boolean
    suspend fun userId(): String?
    fun isAuthenticatedFlow(): Flow<Boolean>

    suspend fun logout()

    suspend fun authenticateWithMobileContentApi(): AuthToken?

    enum class Type { OKTA }
}
