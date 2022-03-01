package org.cru.godtools.base.tool.model

import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.tool.model.Resource

fun Resource.getFileBlocking(fileSystem: ToolFileSystem) = localName?.let { fileSystem.getFileBlocking(it) }
