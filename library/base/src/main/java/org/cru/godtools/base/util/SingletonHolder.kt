package org.cru.godtools.base.util

open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        synchronized(this) {
            return instance ?: creator!!(arg)
                .also {
                    instance = it
                    creator = null
                }
        }
    }
}
