package org.cru.godtools.account.provider

import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered

internal interface AccountProvider : Ordered {
    val type: Type

    suspend fun isAuthenticated(): Boolean
    suspend fun userId(): String?
    fun isAuthenticatedFlow(): Flow<Boolean>

    suspend fun logout()

    enum class Type { OKTA }
}
