package org.cru.godtools.model

import androidx.annotation.RestrictTo
import kotlin.random.Random

typealias Resource = Tool

typealias Lesson = Resource

// TODO: move this to testFixtures once they support Kotlin source files
@RestrictTo(RestrictTo.Scope.TESTS)
fun Resource(
    code: String,
    type: Tool.Type,
    config: Resource.() -> Unit = {},
) = Resource().apply {
    id = Random.nextLong()
    this.code = code
    this.type = type
    config()
}
