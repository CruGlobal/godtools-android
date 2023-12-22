package org.cru.godtools

import kotlin.coroutines.CoroutineContext
import kotlin.test.assertFalse

object TestUtils {
    // HACK: Workaround for https://github.com/robolectric/robolectric/issues/7055#issuecomment-1551119229
    fun clearAndroidUiDispatcher(pkg: String = "androidx.compose.ui.platform") {
        val clazz = javaClass.classLoader!!.loadClass("$pkg.AndroidUiDispatcher")
        val combinedContextClass = javaClass.classLoader!!.loadClass("kotlin.coroutines.CombinedContext")
        val companionClazz = clazz.getDeclaredField("Companion").get(clazz)
        val combinedContext = companionClazz.javaClass.getDeclaredMethod("getMain")
            .invoke(companionClazz) as CoroutineContext

        val androidUiDispatcher = combinedContextClass.getDeclaredField("element")
            .apply { isAccessible = true }
            .get(combinedContext)
            .let { clazz.cast(it) }

        var scheduledFrameDispatch = clazz.getDeclaredField("scheduledFrameDispatch")
            .apply { isAccessible = true }
            .getBoolean(androidUiDispatcher)
        var scheduledTrampolineDispatch = clazz.getDeclaredField("scheduledTrampolineDispatch")
            .apply { isAccessible = true }
            .getBoolean(androidUiDispatcher)

        val dispatchCallback = clazz.getDeclaredField("dispatchCallback")
            .apply { isAccessible = true }
            .get(androidUiDispatcher) as Runnable

        if (scheduledFrameDispatch || scheduledTrampolineDispatch) {
            dispatchCallback.run()
            scheduledFrameDispatch = clazz.getDeclaredField("scheduledFrameDispatch")
                .apply { isAccessible = true }
                .getBoolean(androidUiDispatcher)
            scheduledTrampolineDispatch = clazz.getDeclaredField("scheduledTrampolineDispatch")
                .apply { isAccessible = true }
                .getBoolean(androidUiDispatcher)
        }

        assertFalse(scheduledFrameDispatch)
        assertFalse(scheduledTrampolineDispatch)
    }
}
