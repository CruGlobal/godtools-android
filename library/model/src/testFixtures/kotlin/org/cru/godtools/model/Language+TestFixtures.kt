package org.cru.godtools.model

import java.util.Locale
import kotlin.random.Random
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
fun randomLanguage(
    code: Locale,
    isForcedName: Boolean = Random.nextBoolean(),
    isAdded: Boolean = Random.nextBoolean(),
) = Language(
    code,
    name = Uuid.random().toString().takeIf { Random.nextBoolean() },
    isForcedName = isForcedName,
    isAdded = isAdded,
    apiId = Random.nextLong().takeIf { Random.nextBoolean() }
)
