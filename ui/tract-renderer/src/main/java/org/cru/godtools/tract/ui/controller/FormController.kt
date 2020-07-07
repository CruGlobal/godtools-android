package org.cru.godtools.tract.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import org.cru.godtools.base.model.Event
import org.cru.godtools.tract.databinding.TractContentParagraphBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.xml.model.Form

class FormController private constructor(
    private val binding: TractContentParagraphBinding,
    parentViewHolder: BaseViewHolder<*>?
) : ParentController<Form>(Form::class, binding.content, parentViewHolder) {
    internal constructor(parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) :
        this(TractContentParagraphBinding.inflate(LayoutInflater.from(parent.context), parent, false), parentViewHolder)
    override val contentContainer get() = binding.content

    override fun validate(ids: Set<Event.Id>): Boolean {
        // XXX: right now we only validate if we have a followup:send event
        if (ids.contains(Event.Id.FOLLOWUP_EVENT)) return onValidate()

        // default to default validation logic
        return super.validate(ids)
    }

    override fun buildEvent(builder: Event.Builder): Boolean {
        // we override the default event building process
        onBuildEvent(builder, true)
        return true
    }
}
