package it.vincenzopio.chatpatcher.chat.message;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.chat.ChatQueue;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedChatHandler;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MPatchKeyedHandler implements  com.velocitypowered.proxy.protocol.packet.chat.ChatHandler<KeyedPlayerChat>{
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

        CompletableFuture<MinecraftPacket> chatFuture;
        IdentifiedKey playerKey = this.player.getIdentifiedKey();

        if (playerKey != null && !packet.isUnsigned()) {
            // 1.19->1.19.2 signed version
            chatFuture = future.thenApply(handleOldSignedChat(packet));
        } else {
            // 1.19->1.19.2 unsigned version
            chatFuture = future.thenApply(pme -> {
                PlayerChatEvent.ChatResult chatResult = pme.getResult();
                if (!chatResult.isAllowed()) {
                    return null;
                }

                return player.getChatBuilderFactory().builder()
                        .message(chatResult.getMessage().orElse(packet.getMessage())).setTimestamp(packet.getExpiry()).toServer();
            });
        }
        chatQueue.queuePacket(
                chatFuture.exceptionally((ex) -> {
                    logger.error("Exception while handling player chat for {}", player, ex);
                    return null;
                }),
                packet.getExpiry()
        );
    }

    private Function<PlayerChatEvent, MinecraftPacket> handleOldSignedChat(KeyedPlayerChat packet) {
        IdentifiedKey playerKey = this.player.getIdentifiedKey();
        assert playerKey != null;
        return pme -> {
            PlayerChatEvent.ChatResult chatResult = pme.getResult();

            if (chatResult.getMessage().map(str -> !str.equals(packet.getMessage())).orElse(false)) {
                return player.getChatBuilderFactory().builder()
                        .message(chatResult.getMessage().get() /* always present at this point */)
                        .setTimestamp(packet.getExpiry())
                        .toServer();
            }
            return packet;
        };
    }
}
