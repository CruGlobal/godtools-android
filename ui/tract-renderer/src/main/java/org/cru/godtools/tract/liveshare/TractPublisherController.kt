package org.cru.godtools.tract.liveshare

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.tinder.StateMachine
import com.tinder.scarlet.Scarlet
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.dagger.viewmodel.AssistedSavedStateViewModelFactory
import org.ccci.gto.android.common.scarlet.actioncable.model.Identifier
import org.ccci.gto.android.common.scarlet.actioncable.model.Message
import org.ccci.gto.android.common.scarlet.actioncable.model.Subscribe
import org.cru.godtools.api.TractShareService
import org.cru.godtools.api.TractShareService.Companion.PARAM_CHANNEL_ID
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.api.model.PublisherInfo
import timber.log.Timber
import java.util.UUID

private const val TAG = "TractPublisherContrller"

@OptIn(ExperimentalCoroutinesApi::class)
class TractPublisherController @AssistedInject constructor(
    private val scarlet: Scarlet,
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

    internal val stateMachine = StateMachine.create<State, Event, SideEffect> {
        initialState(State.Initial)
        state<State.Initial> { on<Event.Start> { transitionTo(State.Starting, SideEffect.OpenSocket) } }
        state<State.Starting> { on<Event.Started> { transitionTo(State.On) } }
        state<State.On> { on<Event.Stop> { transitionTo(State.Off) } }
        state<State.Off> { on<Event.Start> { transitionTo(State.On) } }

        onTransition {
            if (it is StateMachine.Transition.Valid) {
                when (it.sideEffect) {
                    SideEffect.OpenSocket -> openSocket()
                }
                state.value = it.toState
            }
        }
    }
    internal val state = MutableLiveData<State>(State.Initial)

    private lateinit var service: TractShareService
    val publisherInfo = MutableLiveData<PublisherInfo>()

    private fun openSocket() {
        viewModelScope.launch {
            service = scarlet.create()

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
                }
            }
            launch {
                service.publisherInfo().consumeEach {
                    publisherInfo.value = it.data
                    stateMachine.transition(Event.Started)
                }
            }
        }
    }

    // private var lastEvent: NavigationEvent? = null
    fun sendNavigationEvent(event: NavigationEvent) {
        if (stateMachine.state == State.On) service.sendEvent(Message(identifier, event))
        // lastEvent = event
    }
}
