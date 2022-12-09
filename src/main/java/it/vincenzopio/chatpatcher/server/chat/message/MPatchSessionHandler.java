package it.vincenzopio.chatpatcher.server.chat.message;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.ChatHandler;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MPatchSessionHandler implements ChatHandler<SessionPlayerChat> {
    private static final Logger logger = LogManager.getLogger(MPatchSessionHandler.class);

    private final ConnectedPlayer player;
    private final VelocityServer server;

    public MPatchSessionHandler(ConnectedPlayer player, VelocityServer server) {
        this.player = player;
        this.server = server;
    }

    @Override
    public Class<SessionPlayerChat> packetClass() {
        return SessionPlayerChat.class;
    }

    @Override
    public void handlePlayerChatInternal(SessionPlayerChat packet) {
        ChatQueue chatQueue = this.player.getChatQueue();
        EventManager eventManager = this.server.getEventManager();
        PlayerChatEvent toSend = new PlayerChatEvent(player, packet.getMessage());
        chatQueue.queuePacket(eventManager.fire(toSend)
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
                }), packet.getTimestamp()
        );
    }
}
