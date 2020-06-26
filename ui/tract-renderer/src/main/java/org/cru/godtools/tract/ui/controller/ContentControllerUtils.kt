package org.cru.godtools.tract.ui.controller

import android.view.ViewGroup
import org.ccci.gto.android.common.app.ApplicationUtils
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.ButtonViewHolder
import org.cru.godtools.tract.viewmodel.FormViewHolder
import org.cru.godtools.tract.viewmodel.ImageViewHolder
import org.cru.godtools.tract.viewmodel.InputViewHolder
import org.cru.godtools.tract.viewmodel.LinkViewHolder
import org.cru.godtools.tract.viewmodel.TabsViewHolder
import org.cru.godtools.tract.viewmodel.TextViewHolder
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
        Button::class -> ButtonViewHolder(parent, parentViewHolder)
        Form::class -> FormViewHolder(parent, parentViewHolder)
        Image::class -> ImageViewHolder(parent, parentViewHolder)
        Input::class -> InputViewHolder(parent, parentViewHolder)
        Link::class -> LinkViewHolder(parent, parentViewHolder)
        Paragraph::class -> ParagraphController(parent, parentViewHolder)
        Tabs::class -> TabsViewHolder(parent, parentViewHolder)
        Text::class -> TextViewHolder(parent, parentViewHolder)
        else -> {
            val e = IllegalArgumentException("Unsupported Content class specified: ${clazz.simpleName}")
            if (ApplicationUtils.isDebuggable(parent.context)) throw e
            Timber.e(e, "Unsupported Content class specified: %s", clazz.simpleName)
            null
        }
    } as BaseViewHolder<T>?
