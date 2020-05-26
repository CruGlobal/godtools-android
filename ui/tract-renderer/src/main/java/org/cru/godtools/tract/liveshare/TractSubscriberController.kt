package org.cru.godtools.tract.liveshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tinder.StateMachine
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.scarlet.actioncable.model.Identifier
import org.ccci.gto.android.common.scarlet.actioncable.model.Subscribe
import org.cru.godtools.api.TractShareService
import org.cru.godtools.api.TractShareService.Companion.CHANNEL_SUBSCRIBER
import org.cru.godtools.api.TractShareService.Companion.PARAM_CHANNEL_ID
import org.cru.godtools.api.model.NavigationEvent
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "TractSubscribrControllr"

class TractSubscriberController @Inject internal constructor(private val scarlet: Scarlet) : ViewModel() {
    var channelId: String? = null
        set(value) {
            if (field == value) return
            field = value
            if (value != null) stateMachine.transition(Event.Start)
        }
    private val identifier get() = Identifier(CHANNEL_SUBSCRIBER, mapOf(PARAM_CHANNEL_ID to channelId))

    private val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.Initial)
        state<State.Initial> { on<Event.Start> { transitionTo(State.On, SideEffect.OpenSocket) } }
        state<State.On> {}

        onTransition {
            if (it is StateMachine.Transition.Valid) {
                when (it.sideEffect) {
                    is SideEffect.OpenSocket -> connectToWebSocket()
                }
            }
        }
    }

    private lateinit var service: TractShareService
    private fun connectToWebSocket() {
        service = scarlet.create()
        viewModelScope.launch {
            launch {
                service.webSocketEvents().consumeEach {
                    when (it) {
                        is WebSocket.Event.OnConnectionOpened<*> -> {
                            service.subscribeToChannel(Subscribe(identifier))
                        }
                        is WebSocket.Event.OnConnectionFailed -> {
                            Timber.tag(TAG).d(it.throwable)
                        }
                    }

                    Timber.tag(TAG).d(it.toString())
                }
            }

            launch(Dispatchers.Main) {
                service.navigationEvents().consumeEach { msg ->
                    receivedEvent.value = msg.data.dataSingle
                }
            }
        }
    }

    val receivedEvent = MutableLiveData<NavigationEvent?>()
}
