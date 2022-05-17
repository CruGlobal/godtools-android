package org.keynote.godtools.android.db

import android.database.MatrixCursor
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.Contract.TrainingTipTable

@RunWith(AndroidJUnit4::class)
@Category(AndroidJUnit4::class)
class TrainingTipMapperTest {
    private val cursor = MatrixCursor(
        arrayOf(
            TrainingTipTable.COLUMN_TOOL,
            TrainingTipTable.COLUMN_LANGUAGE,
            TrainingTipTable.COLUMN_TIP_ID,
            TrainingTipTable.COLUMN_IS_COMPLETED
        )
    )

    @Test
    fun `toObject()`() {
        cursor.newRow()
            .add(TrainingTipTable.COLUMN_TOOL, "test")
            .add(TrainingTipTable.COLUMN_LANGUAGE, "en")
            .add(TrainingTipTable.COLUMN_TIP_ID, "tipId")
            .add(TrainingTipTable.COLUMN_IS_COMPLETED, 1)
        cursor.moveToFirst()

        val tip = TrainingTipMapper.toObject(cursor)
        assertEquals("test", tip.tool)
        assertEquals(Locale.ENGLISH, tip.locale)
        assertEquals("tipId", tip.tipId)
        assertTrue(tip.isCompleted)
    }

    @Test
    fun `toObject() - Invalid Language`() {
        cursor.newRow()
            .add(TrainingTipTable.COLUMN_TOOL, "test")
            .add(TrainingTipTable.COLUMN_LANGUAGE, null)
            .add(TrainingTipTable.COLUMN_TIP_ID, "tipId")
            .add(TrainingTipTable.COLUMN_IS_COMPLETED, 1)
        cursor.moveToFirst()

        val tip = TrainingTipMapper.toObject(cursor)
        assertEquals(Locale.ROOT, tip.locale)
    }
}
