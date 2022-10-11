package org.cru.godtools.account.provider

import android.content.Context
import kotlinx.coroutines.flow.Flow
import org.ccci.gto.android.common.Ordered
import org.cru.godtools.account.model.AccountInfo
import org.cru.godtools.api.model.AuthToken

internal interface AccountProvider : Ordered {
    val type: Type

    suspend fun isAuthenticated(): Boolean
    suspend fun userId(): String?
    fun isAuthenticatedFlow(): Flow<Boolean>
    fun accountInfoFlow(): Flow<AccountInfo?>

    suspend fun login(context: Context)
    suspend fun logout()

    suspend fun authenticateWithMobileContentApi(): AuthToken?

    enum class Type { OKTA }
}
