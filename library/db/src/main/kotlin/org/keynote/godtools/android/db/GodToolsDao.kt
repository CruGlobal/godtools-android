package org.keynote.godtools.android.db

import javax.inject.Inject
import javax.inject.Singleton
import org.ccci.gto.android.common.db.AbstractDao
import org.ccci.gto.android.common.db.CoroutinesAsyncDao
import org.cru.godtools.model.TranslationFile
import org.keynote.godtools.android.db.Contract.TranslationFileTable

@Singleton
class GodToolsDao @Inject internal constructor(database: GodToolsDatabase) : AbstractDao(database), CoroutinesAsyncDao {
    init {
        registerType(
            TranslationFile::class.java,
            TranslationFileTable.TABLE_NAME,
            TranslationFileTable.PROJECTION_ALL,
            TranslationFileMapper,
            TranslationFileTable.SQL_WHERE_PRIMARY_KEY
        )
    }

    public override fun getPrimaryKeyWhere(obj: Any) = when (obj) {
        is TranslationFile -> getPrimaryKeyWhere(TranslationFile::class.java, obj.translationId, obj.filename)
        else -> super.getPrimaryKeyWhere(obj)
    }
}
