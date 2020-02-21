package org.cru.godtools.download.manager

private const val INDETERMINATE_VAL = 0

class DownloadProgress private constructor(progress: Int, max: Int) {
    companion object {
        @JvmField
        val INDETERMINATE = DownloadProgress(INDETERMINATE_VAL, INDETERMINATE_VAL)
    }

    constructor(progress: Long, max: Long) : this(progress.toInt(), max.toInt())

    val max = max.coerceAtLeast(0)
    val progress: Int = progress.coerceAtLeast(0).coerceAtMost(this.max)

    val isIndeterminate get() = max == INDETERMINATE_VAL

    override fun equals(other: Any?) = other is DownloadProgress && progress == other.progress && max == other.max
    override fun hashCode() = intArrayOf(progress, max).contentHashCode()
}
