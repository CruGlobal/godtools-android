package org.keynote.godtools.android.db

import android.database.sqlite.SQLiteOpenHelper
import org.ccci.gto.android.common.db.LiveDataDao
import org.ccci.gto.android.common.db.LiveDataRegistry
import org.ccci.gto.android.common.db.async.AbstractAsyncDao

abstract class GodToolsDaoKotlin(helper: SQLiteOpenHelper) : AbstractAsyncDao(helper), LiveDataDao {
    override val liveDataRegistry = LiveDataRegistry()
}
