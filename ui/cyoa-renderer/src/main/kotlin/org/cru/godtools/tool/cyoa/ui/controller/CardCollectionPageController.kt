package org.cru.godtools.tool.cyoa.ui.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlin.math.abs
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.androidx.viewpager2.widget.currentItemLiveData
import org.ccci.gto.android.common.recyclerview.adapter.SimpleDataBindingAdapter
import org.ccci.gto.android.common.recyclerview.decorator.MarginItemDecoration
import org.cru.godtools.base.tool.ui.controller.BaseController
import org.cru.godtools.base.tool.ui.controller.ParentController
import org.cru.godtools.base.tool.ui.controller.cache.UiControllerCache
import org.cru.godtools.tool.cyoa.R
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionBinding
import org.cru.godtools.tool.cyoa.databinding.CyoaPageCardCollectionCardBinding
import org.cru.godtools.tool.model.page.CardCollectionPage
import org.cru.godtools.tool.model.page.CardCollectionPage.Card
import org.cru.godtools.tool.state.State
import org.greenrobot.eventbus.EventBus

class CardCollectionPageController @AssistedInject constructor(
    @Assisted private val binding: CyoaPageCardCollectionBinding,
    @Assisted override val lifecycleOwner: LifecycleOwner,
    @Assisted override val toolState: State,
    private val cardControllerFactory: CardController.Factory,
    eventBus: EventBus
) : BaseController<CardCollectionPage>(CardCollectionPage::class, binding.root, eventBus = eventBus) {
    @AssistedFactory
    interface Factory {
        fun create(
            binding: CyoaPageCardCollectionBinding,
            lifecycleOwner: LifecycleOwner,
            toolState: State
        ): CardCollectionPageController
    }

    override fun onBind() {
        super.onBind()
        binding.page = model
        adapter.cards = model?.cards.orEmpty()
    }


    // region Cards ViewPager
    private val adapter = CyoaCardCollectionPageCardDataBindingAdapter()

    init {
        with(binding.cards) {
            adapter = this@CardCollectionPageController.adapter
            offscreenPageLimit = 1

            val gap = resources.getDimensionPixelSize(R.dimen.cyoa_page_cardcollection_card_gap)
            val peek = resources.getDimensionPixelSize(R.dimen.cyoa_page_cardcollection_card_peek)
            addItemDecoration(MarginItemDecoration(horizontalMargins = gap + peek))
            setPageTransformer { p, pos ->
                val rawScale = 0.25f * pos
                val scale = 1 - abs(rawScale)
                p.scaleX = scale
                p.scaleY = scale
                p.translationX = (-(2 * peek + gap) * pos) - (rawScale * p.measuredWidth / 2)
            }

            binding.currentCardIndex = currentItemLiveData
        }
    }

    inner class CyoaCardCollectionPageCardDataBindingAdapter :
        SimpleDataBindingAdapter<CyoaPageCardCollectionCardBinding>() {
        var cards = emptyList<Card>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        override fun getItemCount() = cards.size
        override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int): CyoaPageCardCollectionCardBinding {
            return cardControllerFactory.create(parent, this@CardCollectionPageController).binding
        }

        override fun onViewDataBindingCreated(binding: CyoaPageCardCollectionCardBinding, viewType: Int) {
            binding.lifecycleOwner = binding.controller?.lifecycleOwner
        }

        override fun onBindViewDataBinding(binding: CyoaPageCardCollectionCardBinding, position: Int) {
            binding.controller?.model = cards[position]
        }
    }
    // endregion Cards ViewPager

    class CardController private constructor(
        val binding: CyoaPageCardCollectionCardBinding,
        parentController: BaseController<*>,
        cacheFactory: UiControllerCache.Factory
    ) : ParentController<Card>(Card::class, binding.root, parentController, cacheFactory) {
        @AssistedInject
        internal constructor(
            @Assisted parent: ViewGroup,
            @Assisted parentController: BaseController<*>,
            cacheFactory: UiControllerCache.Factory
        ) : this(
            CyoaPageCardCollectionCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            parentController,
            cacheFactory
        )

        @AssistedFactory
        interface Factory : BaseController.Factory<CardController>

        override val lifecycleOwner =
            super.lifecycleOwner?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }
        override val childContainer get() = binding.content

        init {
            binding.lifecycleOwner = lifecycleOwner
            binding.controller = this
        }
    }
}

fun CyoaPageCardCollectionBinding.bindController(
    factory: CardCollectionPageController.Factory,
    lifecycleOwner: LifecycleOwner,
    toolState: State
) = controller ?: factory.create(this, lifecycleOwner, toolState)
