package org.cru.godtools.db.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.cru.godtools.model.GlobalActivityAnalytics

@Entity(tableName = "global_activity")
internal class GlobalActivityEntity(
    val users: Int = 0,
    val countries: Int = 0,
    val launches: Int = 0,
    val gospelPresentations: Int = 0,
) {
    companion object {
        const val ID = 1
    }

    constructor(model: GlobalActivityAnalytics) : this(
        users = model.users,
        countries = model.countries,
        launches = model.launches,
        gospelPresentations = model.gospelPresentations
    )

    @PrimaryKey
    var id = ID

    fun toModel() = GlobalActivityAnalytics().also {
        it.users = users
        it.countries = countries
        it.launches = launches
        it.gospelPresentations = gospelPresentations
    }
}
