package org.cru.godtools.ui.tools

import io.mockk.mockk

@Suppress("ktlint:standard:function-naming")
fun FakeToolCardPresenter() = ToolCardPresenter(
    fileSystem = mockk(),
    settings = mockk(relaxed = true),
    attachmentsRepository = mockk(relaxed = true),
    languagesRepository = mockk(relaxed = true),
    toolsRepository = mockk(),
    translationsRepository = mockk(relaxed = true),
)
