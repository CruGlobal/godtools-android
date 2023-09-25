package org.cru.godtools.db.repository

import kotlinx.coroutines.flow.Flow
import org.cru.godtools.model.User

interface UserRepository {
    suspend fun findUser(userId: String): User?
    fun findUserFlow(userId: String): Flow<User?>

    // region Sync Methods
    suspend fun storeUserFromSync(user: User)
    // endregion Sync Methods
}
