package org.cru.godtools.base.tool.model

import io.fluidsonic.locale.toPlatform
import org.cru.godtools.shared.tool.parser.model.Manifest

val Manifest?.platformLocale get() = this?.locale?.toPlatform()
