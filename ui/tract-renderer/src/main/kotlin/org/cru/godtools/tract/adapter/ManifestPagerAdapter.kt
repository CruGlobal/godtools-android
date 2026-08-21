package org.cru.godtools.tract.adapter

import android.os.Bundle
import android.os.Parcelable
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.core.content.res.use
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.setViewTreeLifecycleOwner
import com.karumi.weak.weak
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Named
import kotlinx.coroutines.flow.combine
import okio.FileSystem
import org.ccci.gto.android.common.androidx.lifecycle.ConstrainedStateLifecycleOwner
import org.ccci.gto.android.common.eventbus.lifecycle.register
import org.ccci.gto.android.common.util.Ids
import org.ccci.gto.android.common.viewpager.adapter.ViewHolderPagerAdapter
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_CLICKED
import org.cru.godtools.base.Settings.Companion.FEATURE_TRACT_CARD_SWIPED
import org.cru.godtools.base.tool.BaseToolRendererModule.Companion.TOOL_RESOURCE_FILE_SYSTEM
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.shared.renderer.state.State
import org.cru.godtools.shared.renderer.tips.TipsRepository
import org.cru.godtools.shared.renderer.tract.RenderTractPage
import org.cru.godtools.shared.renderer.tract.TractPageEvent
import org.cru.godtools.shared.renderer.tract.TractPageState
import org.cru.godtools.shared.renderer.util.ProvideRendererServices
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.tract.Modal
import org.cru.godtools.shared.tool.parser.model.tract.TractPage
import org.cru.godtools.shared.tool.parser.model.tract.TractPage.Card
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

private const val STATE_PAGE_STATE = "pageState"

class ManifestPagerAdapter @AssistedInject internal constructor(
    @Assisted lifecycleOwner: LifecycleOwner,
    @Assisted private val toolState: State,
    @param:Named(TOOL_RESOURCE_FILE_SYSTEM) private val resourceFileSystem: FileSystem,
    private val settings: Settings,
    private val tipsRepository: TipsRepository,
    eventBus: EventBus
) : ViewHolderPagerAdapter<ManifestPagerAdapter.PageViewHolder>() {
    @AssistedFactory
    interface Factory {
        fun create(lifecycleOwner: LifecycleOwner, toolState: State): ManifestPagerAdapter
    }

    interface Callbacks {
        fun onUpdateActiveCard(page: TractPage, card: Card?)
        fun showModal(modal: Modal)
        fun goToPage(position: Int)
    }

    private val lifecycleOwner by weak(lifecycleOwner)

    internal var manifest: Manifest? = null
        set(value) {
            val changed = field !== value
            field = value
            if (changed) notifyDataSetChanged()
        }
    var callbacks: Callbacks? = null

    init {
        setHasStableIds(true)
        eventBus.register(lifecycleOwner, this)
    }

    override fun getCount() = manifest?.pages?.size ?: 0
    private fun getItem(position: Int) = manifest?.pages?.getOrNull(position) as? TractPage
    override fun getItemId(position: Int) = getItem(position)?.id?.let { Ids.generate(it) } ?: NO_ID
    override fun getItemPositionFromId(id: Long) = manifest?.pages
        ?.indexOfFirst { id == Ids.generate(it.id) }
        ?.takeUnless { it < 0 }
        ?: POSITION_NONE

    internal val primaryPage get() = primaryItem?.page
    internal val primaryPageActiveCard get() = primaryItem?.pageState?.activeCard

    // region TractPageState
    private val pageStates = mutableMapOf<String, TractPageState>()
    private fun pageState(page: TractPage) =
        pageStates.getOrPut(page.id) { TractPageState(page) }.also { it.updatePage(page) }
    // endregion TractPageState

    // region Lifecycle
    override fun onCreateViewHolder(parent: ViewGroup) = PageViewHolder(ComposeView(parent.context))

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val page = getItem(position)
        holder.isPageStateFresh = page != null && page.id !in pageStates
        holder.pageState = page?.let { pageState(it) }
        holder.lifecycleOwner?.apply { maxState = maxOf(maxState, Lifecycle.State.STARTED) }
    }

    override fun onViewHolderRecycled(holder: PageViewHolder) {
        super.onViewHolderRecycled(holder)
        holder.lifecycleOwner?.maxState = Lifecycle.State.CREATED
        holder.pageState = null
    }

    override fun onUpdatePrimaryItem(old: PageViewHolder?, holder: PageViewHolder?) {
        holder?.pageState?.let { callbacks?.onUpdateActiveCard(it.page, it.activeCard) }

        if (old !== holder) {
            old?.lifecycleOwner?.maxState = Lifecycle.State.STARTED
            holder?.lifecycleOwner?.maxState = Lifecycle.State.RESUMED
        }
    }

    @MainThread
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun onLiveShareNavigationEvent(event: NavigationEvent) {
        val holder = primaryItem ?: return
        val page = holder.page?.takeIf { it.position == event.page } ?: return
        holder.pageState?.navigateToCard(event.card?.let { page.cards.getOrNull(it) })
    }
    // endregion Lifecycle

    private fun onPageEvent(holder: PageViewHolder, event: TractPageEvent) {
        when (event) {
            is TractPageEvent.OpenModal -> callbacks?.showModal(event.modal)
            TractPageEvent.GoToNextPage -> holder.page?.let { callbacks?.goToPage(it.position + 1) }
            TractPageEvent.CardTapped -> settings.setFeatureDiscovered(FEATURE_TRACT_CARD_CLICKED)
            TractPageEvent.CardSwiped -> settings.setFeatureDiscovered(FEATURE_TRACT_CARD_SWIPED)
        }
    }

    inner class PageViewHolder internal constructor(composeView: ComposeView) : ViewHolder(composeView) {
        internal val lifecycleOwner = this@ManifestPagerAdapter.lifecycleOwner
            ?.let { ConstrainedStateLifecycleOwner(it, Lifecycle.State.CREATED) }

        internal var pageState: TractPageState? by mutableStateOf(null)
        internal val page get() = pageState?.page

        /** true when the bound pageState was freshly created and doesn't contain any live state yet */
        internal var isPageStateFresh = false

        private val actionBarSize = composeView.context.theme
            .obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
            .use { it.getDimensionPixelSize(0, 0) }

        init {
            composeView.apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setContent {
                    GodToolsTheme(darkTheme = false) {
                        ProvideRendererServices(resourceFileSystem, tipsRepository = tipsRepository) {
                            PageContent()
                        }
                    }
                }
            }
        }

        @Composable
        private fun PageContent() {
            val pageState = pageState ?: return

            UpdateActiveCardEffect(pageState)
            BounceFirstCardEffect(pageState)
            RenderTractPage(
                pageState,
                contentInsets = with(LocalDensity.current) { PaddingValues(top = actionBarSize.toDp()) },
                state = toolState,
                pageEvents = { onPageEvent(this@PageViewHolder, it) }
            )
        }

        @Composable
        private fun UpdateActiveCardEffect(pageState: TractPageState) {
            LaunchedEffect(pageState) {
                snapshotFlow { pageState.activeCard }
                    .collect {
                        if (this@PageViewHolder === primaryItem) callbacks?.onUpdateActiveCard(pageState.page, it)
                    }
            }
        }

        @Composable
        private fun BounceFirstCardEffect(pageState: TractPageState) {
            val cardsDiscovered by remember {
                settings.isFeatureDiscoveredFlow(FEATURE_TRACT_CARD_CLICKED)
                    .combine(settings.isFeatureDiscoveredFlow(FEATURE_TRACT_CARD_SWIPED)) { c, s -> c || s }
            }.collectAsState(true)
            val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()

            val bounce = lifecycleState.isAtLeast(Lifecycle.State.RESUMED) && !cardsDiscovered
            LaunchedEffect(pageState, bounce) { pageState.isBounceFirstCard = bounce }
        }

        // region ViewHolder State
        override fun saveState(): Parcelable = Bundle().apply {
            val pageState = pageState ?: return@apply
            val saved =
                with(TractPageState.Saver(pageState.page)) { SaverScope { true }.save(pageState) } as? List<*>
            saved?.let { putSerializable(STATE_PAGE_STATE, ArrayList(it)) }
        }

        override fun restoreState(state: Parcelable?) {
            // an existing TractPageState is the live source of truth, don't replace it with stale saved state
            if (!isPageStateFresh) return
            val page = pageState?.page ?: return
            @Suppress("DEPRECATION")
            val saved = (state as? Bundle)?.getSerializable(STATE_PAGE_STATE) as? ArrayList<*> ?: return
            TractPageState.Saver(page).restore(saved)?.let {
                pageStates[page.id] = it
                pageState = it
                isPageStateFresh = false
            }
        }
        // endregion ViewHolder State
    }
}
