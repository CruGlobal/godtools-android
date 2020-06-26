package org.cru.godtools.tract.ui.controller

import org.cru.godtools.tract.databinding.TractPageBinding
import org.cru.godtools.tract.viewmodel.BaseViewHolder
import org.cru.godtools.tract.viewmodel.PageViewHolder

// TODO: this may change once I figure out what code pattern I want to use to create/bind controllers
fun TractPageBinding.bindController() =
    BaseViewHolder.forView(root, PageViewHolder::class.java) ?: PageViewHolder(this)
