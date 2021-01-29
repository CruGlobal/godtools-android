package org.cru.godtools.ui.tools

import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.databinding.ObservableField
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.karumi.weak.weak
import org.ccci.gto.android.common.recyclerview.advrecyclerview.draggable.DataBindingDraggableItemViewHolder
import org.ccci.gto.android.common.recyclerview.advrecyclerview.draggable.SimpleDataBindingDraggableItemAdapter
import org.cru.godtools.databinding.ToolsListItemToolBinding
import org.cru.godtools.model.Tool

private typealias VH = DataBindingDraggableItemViewHolder<ToolsListItemToolBinding>

class ToolsAdapter(lifecycleOwner: LifecycleOwner, viewModelProvider: ViewModelProvider) :
    SimpleDataBindingDraggableItemAdapter<ToolsListItemToolBinding>(lifecycleOwner), Observer<List<Tool>> {
    init {
        setHasStableIds(true)
    }

    private val viewModel = viewModelProvider.get(ToolsAdapterViewModel::class.java)

    val callbacks = ObservableField<ToolsAdapterCallbacks>()
    private var tools: List<Tool>? = null
        set(value) {
            field = value
            reordering = value?.indices?.toList()?.toIntArray() ?: IntArray(0)
            notifyDataSetChanged()
        }

    override fun getItemCount() = tools?.size ?: 0
    private fun getItem(position: Int) = tools?.get(reordering[position])
    override fun getItemId(position: Int) = getItem(position)?.id ?: NO_ID

    // region Lifecycle
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
    }

    override fun onChanged(t: List<Tool>?) {
        tools = t
    }

    override fun onCreateViewDataBinding(parent: ViewGroup, viewType: Int) =
        ToolsListItemToolBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            .also { it.callbacks = callbacks }

    override fun onBindViewDataBinding(binding: ToolsListItemToolBinding, position: Int) {
        val tool = getItem(position)
        val toolViewModel = tool?.code?.let { viewModel.getToolViewModel(it) }

        binding.tool = tool
        binding.setDownloadProgress(toolViewModel?.downloadProgress)
        binding.setBanner(toolViewModel?.banner)
        binding.primaryTranslation = toolViewModel?.firstTranslation
        binding.parallelTranslation = toolViewModel?.parallelTranslation
        binding.parallelLanguage = toolViewModel?.parallelLanguage
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = this.recyclerView?.takeUnless { it == recyclerView }
    }
    // endregion Lifecycle

    // region DraggableItemAdapter
    private var recyclerView: RecyclerView? by weak()

    override fun onCheckCanStartDrag(holder: VH, position: Int, x: Int, y: Int) = true
    override fun onGetItemDraggableRange(holder: VH, position: Int): ItemDraggableRange? = null

    override fun onItemDragStarted(position: Int) {
        // perform haptic feedback
        recyclerView?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int) = true
    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) = Unit
    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        updateToolPosition(fromPosition, toPosition)
        triggerToolOrderUpdate()
    }
    // endregion DraggableItemAdapter

    // region Reordering
    private var reordering = IntArray(0)

    private fun updateToolPosition(fromPosition: Int, toPosition: Int) {
        // short-circuit if the position isn't actually changing
        if (fromPosition == toPosition) return

        // short-circuit if there are invalid positions
        if (!reordering.indices.contains(fromPosition) || !reordering.indices.contains(toPosition)) return

        val tmp = reordering[fromPosition]
        if (fromPosition < toPosition) {
            // 0123F56T8 -> 0123_56T8 -> 012356T_8 -> 012356TF8
            System.arraycopy(reordering, fromPosition + 1, reordering, fromPosition, toPosition - fromPosition)
        } else {
            // 0123T56F8 -> 0123T56_8 -> 0123_T568 -> 0123FT568
            System.arraycopy(reordering, toPosition, reordering, toPosition + 1, fromPosition - toPosition)
        }
        reordering[toPosition] = tmp
    }

    @MainThread
    private fun triggerToolOrderUpdate() {
        callbacks.get()?.onToolsReordered(*(0 until itemCount).map { getItemId(it) }.toLongArray())
    }
    // endregion Reordering
}
