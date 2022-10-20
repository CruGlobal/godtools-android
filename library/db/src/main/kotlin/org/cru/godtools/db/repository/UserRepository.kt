package org.cru.godtools.db.repository

import org.cru.godtools.model.User

interface UserRepository {
    // region Sync Methods
    suspend fun storeUserFromSync(user: User)
    // endregion Sync Methods
}
