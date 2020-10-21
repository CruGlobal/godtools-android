package org.cru.godtools.tract.liveshare

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.StateMachine
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.scarlet.ReferenceLifecycle
import org.ccci.gto.android.common.scarlet.actioncable.model.Identifier
import org.ccci.gto.android.common.scarlet.actioncable.model.Subscribe
import org.ccci.gto.android.common.scarlet.actioncable.model.Unsubscribe
import org.cru.godtools.api.TractShareService
import org.cru.godtools.api.TractShareService.Companion.CHANNEL_SUBSCRIBER
import org.cru.godtools.api.TractShareService.Companion.PARAM_CHANNEL_ID
import org.cru.godtools.api.model.NavigationEvent
import timber.log.Timber

private const val TAG = "TractSubscribrControllr"

@OptIn(ExperimentalCoroutinesApi::class)
class TractSubscriberController @ViewModelInject internal constructor(
    private val service: TractShareService,
    private val referenceLifecycle: ReferenceLifecycle
) : ViewModel() {
    var channelId: String? = null
        set(value) {
            if (field == value) return
            field = value
            if (value != null) stateMachine.transition(Event.Start)
        }
    private val identifier get() = Identifier(CHANNEL_SUBSCRIBER, mapOf(PARAM_CHANNEL_ID to channelId))

    internal val state = MutableLiveData<State>(State.Off)
    private val stateMachine = StateMachine.create<State, Event, Unit> {
        initialState(State.Off)
        state<State.Off> { on<Event.Start> { transitionTo(State.On) } }
        state<State.On> {
            var jobs: Job? = null

            onEnter {
                referenceLifecycle.acquire(this@TractSubscriberController)
                jobs = viewModelScope.launch {
                    val socketEventsChannel = service.webSocketEvents()
                    val navigationEventsChannel = service.navigationEvents()

                    launch {
                        try {
                            service.subscribe(Subscribe(identifier))
                            socketEventsChannel.consumeEach {
                                when (it) {
                                    is WebSocket.Event.OnConnectionOpened<*> -> service.subscribe(Subscribe(identifier))
                                    is WebSocket.Event.OnConnectionFailed -> Timber.tag(TAG).d(it.throwable)
                                }
                            }
                        } finally {
                            service.unsubscribe(Unsubscribe(identifier))
                        }
                    }

                    launch(Dispatchers.Main) {
                        navigationEventsChannel.consumeEach {
                            if (it.identifier == identifier) receivedEvent.value = it.data
                        }
                    }
                }
            }

            on<Event.Stop> { transitionTo(State.Off) }

            onExit {
                jobs?.cancel()
                jobs = null
                referenceLifecycle.release(this@TractSubscriberController)
            }
        }

        onTransition { if (it is StateMachine.Transition.Valid) state.value = it.toState }
    }

    val receivedEvent = MutableLiveData<NavigationEvent?>()

    override fun onCleared() {
        super.onCleared()
        stateMachine.transition(Event.Stop)
    }
}
