package org.cru.godtools.xml.model

internal const val TOOL_CODE = "test"

internal fun mockManifest() = Manifest(TOOL_CODE)
internal fun mockPage(manifest: Manifest = mockManifest()) = Page(manifest, 0)
