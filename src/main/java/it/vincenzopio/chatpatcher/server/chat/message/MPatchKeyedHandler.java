package it.vincenzopio.chatpatcher.server.chat.message;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChatPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.concurrent.CompletableFuture;

public class MPatchKeyedHandler implements com.velocitypowered.proxy.protocol.packet.chat.ChatHandler<KeyedPlayerChatPacket> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MPatchKeyedHandler.class);

    private final VelocityServer server;
    private final ConnectedPlayer player;

    public MPatchKeyedHandler(VelocityServer server, ConnectedPlayer player) {
        this.server = server;
        this.player = player;
    }

    @Override
    public Class<KeyedPlayerChatPacket> packetClass() {
        return KeyedPlayerChatPacket.class;
    }


    @Override
    public void handlePlayerChatInternal(KeyedPlayerChatPacket packet) {
        ChatQueue chatQueue = this.player.getChatQueue();
        EventManager eventManager = this.server.getEventManager();
        PlayerChatEvent toSend = new PlayerChatEvent(player, packet.getMessage());
        CompletableFuture<PlayerChatEvent> future = eventManager.fire(toSend);

        CompletableFuture<MinecraftPacket> chatFuture = future.thenApply(pme -> {
            PlayerChatEvent.ChatResult chatResult = pme.getResult();
            if (!chatResult.isAllowed()) {
                return null;
            }

            return player.getChatBuilderFactory().builder()
                    .message(chatResult.getMessage().orElse(packet.getMessage()))
                    .setTimestamp(packet.getExpiry())
                    .toServer();
        });

        chatQueue.queuePacket(chatFuture.exceptionally((ex) -> {
                    LOGGER.error("Exception while handling player chat for {}", player, ex);
                    return null;
                }), packet.getExpiry()
        );
    }
}
