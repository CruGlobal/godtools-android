package org.cru.godtools.tract.liveshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.StateMachine
import com.tinder.scarlet.WebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
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

private const val TAG = "TractPublisherContrller"

@HiltViewModel
class TractPublisherController @Inject constructor(
    private val service: TractShareService,
    private val referenceLifecycle: ReferenceLifecycle,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
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
                            subscriptionChannel.consumeEach {
                                if (it.identifier == identifier) lastEvent?.let { sendNavigationEvent(it) }
                            }
                        } finally {
                            service.unsubscribe(Unsubscribe(identifier))
                        }
                    }

                    launch {
                        publisherInfoChannel.consumeEach {
                            if (it.identifier == identifier) publisherInfo.value = it.data
                        }
                    }

                    launch {
                        socketEventsChannel.consumeEach {
                            when (it) {
                                is WebSocket.Event.OnConnectionOpened<*> -> service.subscribe(Subscribe(identifier))
                                is WebSocket.Event.OnConnectionFailed -> Timber.tag(TAG).d(it.throwable)
                                else -> Unit
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
