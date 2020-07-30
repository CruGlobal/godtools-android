package org.cru.godtools.tract.liveshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.tinder.StateMachine
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.scarlet.ReferenceLifecycle
import org.ccci.gto.android.common.scarlet.actioncable.model.Identifier
import org.ccci.gto.android.common.scarlet.actioncable.model.Message
import org.ccci.gto.android.common.scarlet.actioncable.model.Subscribe
import org.ccci.gto.android.common.scarlet.actioncable.model.Unsubscribe
import org.cru.godtools.api.TractShareService
import org.cru.godtools.api.TractShareService.Companion.PARAM_CHANNEL_ID
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.api.model.PublisherInfo
import timber.log.Timber
import java.util.UUID

private const val TAG = "TractPublisherContrller"

@OptIn(ExperimentalCoroutinesApi::class)
class TractPublisherController @AssistedInject constructor(
    private val service: TractShareService,
    private val referenceLifecycle: ReferenceLifecycle,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    @AssistedInject.Factory
    interface Factory : AssistedSavedStateViewModelFactory<TractPublisherController>

    private val channelId: String
        get() = savedStateHandle[PARAM_CHANNEL_ID]
            ?: UUID.randomUUID().toString().also { savedStateHandle[PARAM_CHANNEL_ID] = it }
    private val identifier by lazy {
        Identifier(TractShareService.CHANNEL_PUBLISHER, mapOf(PARAM_CHANNEL_ID to channelId))
    }

    var started: Boolean
        get() = savedStateHandle["started"] ?: false
        set(value) {
            savedStateHandle["started"] = value
            if (value) stateMachine.transition(Event.Start) else stateMachine.transition(Event.Stop)
        }

    internal val state = MutableLiveData<State>(State.Off)
    private val stateMachine = StateMachine.create<State, Event, Unit> {
        initialState(State.Off)
        state<State.Off> { on<Event.Start> { transitionTo(State.On) } }
        state<State.On> {
            var jobs: Job? = null
            onEnter {
                referenceLifecycle.acquire(this@TractPublisherController)
                jobs = viewModelScope.launch {
                    val socketEventsChannel = service.webSocketEvents()
                    val subscriptionChannel = service.subscriptionConfirmation()
                    val publisherInfoChannel = service.publisherInfo()

                    launch {
                        try {
                            service.subscribe(Subscribe(identifier))
                            subscriptionChannel
                                .filter { it.identifier == identifier }
                                .consumeEach { lastEvent?.let { sendNavigationEvent(it) } }
                        } finally {
                            service.unsubscribe(Unsubscribe(identifier))
                        }
                    }

                    launch {
                        publisherInfoChannel
                            .filter { it.identifier == identifier }
                            .consumeEach { publisherInfo.value = it.data }
                    }

                    launch {
                        socketEventsChannel.consumeEach {
                            when (it) {
                                is WebSocket.Event.OnConnectionOpened<*> -> service.subscribe(Subscribe(identifier))
                                is WebSocket.Event.OnConnectionFailed -> Timber.tag(TAG).d(it.throwable)
                            }
                        }
                    }
                }
            }

            on<Event.Stop> { transitionTo(State.Off) }

            onExit {
                jobs?.cancel()
                jobs = null
                referenceLifecycle.release(this@TractPublisherController)
            }
        }

        onTransition { if (it is StateMachine.Transition.Valid) state.value = it.toState }
    }

    init {
        if (started) stateMachine.transition(Event.Start)
    }

    val publisherInfo = MutableLiveData<PublisherInfo>()

    private var lastEvent: NavigationEvent? = null
    fun sendNavigationEvent(event: NavigationEvent) {
        if (stateMachine.state == State.On) service.sendEvent(Message(identifier, event))
        lastEvent = event
    }

    override fun onCleared() {
        super.onCleared()
        stateMachine.transition(Event.Stop)
    }
}
