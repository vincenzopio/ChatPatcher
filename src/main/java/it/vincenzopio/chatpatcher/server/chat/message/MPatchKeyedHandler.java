package it.vincenzopio.chatpatcher.server.chat.message;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

public class MPatchKeyedHandler implements com.velocitypowered.proxy.protocol.packet.chat.ChatHandler<KeyedPlayerChat> {
    private static final Logger logger = LogManager.getLogger(MPatchKeyedHandler.class);

    private final VelocityServer server;
    private final ConnectedPlayer player;

    public MPatchKeyedHandler(VelocityServer server, ConnectedPlayer player) {
        this.server = server;
        this.player = player;
    }

    @Override
    public Class<KeyedPlayerChat> packetClass() {
        return KeyedPlayerChat.class;
    }


    @Override
    public void handlePlayerChatInternal(KeyedPlayerChat packet) {
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
                    logger.error("Exception while handling player chat for {}", player, ex);
                    return null;
                }), packet.getExpiry()
        );
    }
}
