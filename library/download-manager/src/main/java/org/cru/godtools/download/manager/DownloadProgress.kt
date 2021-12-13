package org.cru.godtools.download.manager

class DownloadProgress(progress: Long, max: Long) {
    internal companion object {
        internal const val INDETERMINATE_VAL = 0L

        internal val INITIAL = DownloadProgress(INDETERMINATE_VAL, INDETERMINATE_VAL)
    }

    val max = max.toInt().coerceAtLeast(0)
    val progress: Int = progress.toInt().coerceAtLeast(0).coerceAtMost(this.max)

    val isIndeterminate get() = max == INDETERMINATE_VAL.toInt()

    override fun equals(other: Any?) = other is DownloadProgress && progress == other.progress && max == other.max
    override fun hashCode() = intArrayOf(progress, max).contentHashCode()
}
