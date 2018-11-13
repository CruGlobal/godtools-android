package org.cru.godtools.util

import com.annimon.stream.Stream

fun <T> Stream<T>.asSequence(): Sequence<T> {
    return Sequence { iterator() }
}
