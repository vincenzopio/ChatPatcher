package it.vincenzopio.chatpatcher.server.chat.message;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.ChatHandler;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;

public final class MPatchSessionHandler implements ChatHandler<SessionPlayerChatPacket> {

    private final ConnectedPlayer player;
    private final VelocityServer server;

    public MPatchSessionHandler(ConnectedPlayer player, VelocityServer server) {
        this.player = player;
        this.server = server;
    }

    @Override
    public Class<SessionPlayerChatPacket> packetClass() {
        return SessionPlayerChatPacket.class;
    }

    @Override
    public void handlePlayerChatInternal(SessionPlayerChatPacket packet) {
        ChatQueue chatQueue = this.player.getChatQueue();
        EventManager eventManager = this.server.getEventManager();
        PlayerChatEvent toSend = new PlayerChatEvent(player, packet.getMessage());

        chatQueue.queuePacket(item -> eventManager.fire(toSend)
                .thenApply(pme -> {
                    PlayerChatEvent.ChatResult chatResult = pme.getResult();
                    if (!chatResult.isAllowed()) {
                        return null;
                    }

                    String message = chatResult.getMessage().orElse(packet.getMessage());

                    return this.player.getChatBuilderFactory().builder()
                            .message(message)
                            .setTimestamp(packet.getTimestamp())
                            .toServer();
                }), packet.getTimestamp(), null);
    }
}
