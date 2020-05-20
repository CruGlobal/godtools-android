package org.cru.godtools.api

import com.tinder.scarlet.WebSocket
import com.tinder.scarlet.ws.Receive
import com.tinder.scarlet.ws.Send
import kotlinx.coroutines.channels.ReceiveChannel
import org.ccci.gto.android.common.scarlet.actioncable.ActionCableChannel
import org.ccci.gto.android.common.scarlet.actioncable.model.Message
import org.ccci.gto.android.common.scarlet.actioncable.model.Subscribe
import org.cru.godtools.api.model.NavigationEvent
import org.cru.godtools.api.model.PublisherInfo

interface TractShareService {
    companion object {
        const val CHANNEL_PUBLISHER = "PublishChannel"
        const val CHANNEL_SUBSCRIBER = "SubscribeChannel"

        const val PARAM_CHANNEL_ID = "channelId"
    }

    @Receive
    fun webSocketEvents(): ReceiveChannel<WebSocket.Event>

    @Send
    fun subscribeToChannel(subscribe: Subscribe)

    @Receive
    @ActionCableChannel(CHANNEL_PUBLISHER)
    fun publisherInfo(): ReceiveChannel<Message<PublisherInfo>>

    @Send
    @ActionCableChannel(CHANNEL_PUBLISHER)
    fun sendEvent(event: Message<NavigationEvent>)

    @Receive
    @ActionCableChannel(CHANNEL_SUBSCRIBER)
    fun navigationEvents(): ReceiveChannel<Message<NavigationEvent>>
}
