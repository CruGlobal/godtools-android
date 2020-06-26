package org.cru.godtools.tract.ui.controller

import android.view.ViewGroup
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.InputViewHolder
import org.cru.godtools.tract.viewmodel.TabsViewHolder
import org.cru.godtools.xml.model.Button
import org.cru.godtools.xml.model.Content
import org.cru.godtools.xml.model.Form
import org.cru.godtools.xml.model.Image
import org.cru.godtools.xml.model.Input
import org.cru.godtools.xml.model.Link
import org.cru.godtools.xml.model.Paragraph
import org.cru.godtools.xml.model.Tabs
import org.cru.godtools.xml.model.Text
import timber.log.Timber
import kotlin.reflect.KClass

internal fun <T : Content> createController(clazz: KClass<T>, parent: ViewGroup, parentViewHolder: BaseViewHolder<*>?) =
    when (clazz) {
        Button::class -> ButtonController(parent, parentViewHolder)
        Form::class -> FormController(parent, parentViewHolder)
        Image::class -> ImageController(parent, parentViewHolder)
        Input::class -> InputViewHolder(parent, parentViewHolder)
        Link::class -> LinkController(parent, parentViewHolder)
        Paragraph::class -> ParagraphController(parent, parentViewHolder)
        Tabs::class -> TabsViewHolder(parent, parentViewHolder)
        Text::class -> TextController(parent, parentViewHolder)
        else -> {
            val e = IllegalArgumentException("Unsupported Content class specified: ${clazz.simpleName}")
            if (ApplicationUtils.isDebuggable(parent.context)) throw e
            Timber.e(e, "Unsupported Content class specified: %s", clazz.simpleName)
            null
        }
    } as BaseViewHolder<T>?
