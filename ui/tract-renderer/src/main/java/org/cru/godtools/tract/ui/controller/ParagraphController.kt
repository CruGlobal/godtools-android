package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.tract.databinding.TractContentParagraphBinding
import org.cru.godtools.xml.model.Paragraph

class ParagraphController private constructor(
    private val binding: TractContentParagraphBinding,
    parentController: BaseController<*>?
) : ParentController<Paragraph>(Paragraph::class, binding.content, parentController) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseController<*>?) :
        this(TractContentParagraphBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)

    override val contentContainer get() = binding.content
}
