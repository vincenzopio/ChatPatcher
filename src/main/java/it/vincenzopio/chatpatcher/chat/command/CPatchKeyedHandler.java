package it.vincenzopio.chatpatcher.chat.command;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerCommand;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class CPatchKeyedHandler implements CommandHandler<KeyedPlayerCommand> {
    private final ConnectedPlayer player;
    private final VelocityServer server;

    public CPatchKeyedHandler(ConnectedPlayer player, VelocityServer server) {
        this.player = player;
        this.server = server;
    }

    @Override
    public Class<KeyedPlayerCommand> packetClass() {
        return KeyedPlayerCommand.class;
    }

    @Override
    public void handlePlayerCommandInternal(KeyedPlayerCommand packet) {
        EventManager eventManager = this.server.getEventManager();

        String message = packet.getCommand();

        player.getChatQueue().queuePacket(eventManager.fire(new CommandExecuteEvent(player, message)).thenComposeAsync(event -> {
                CommandExecuteEvent.CommandResult result = event.getResult();

                if (result == CommandExecuteEvent.CommandResult.denied()) {
                    return CompletableFuture.completedFuture(null);
                }

                String commandToRun = result.getCommand().orElse(message);
                if (result.isForwardToServer()) {
                    ChatBuilderV2 write = this.player.getChatBuilderFactory()
                            .builder()
                            .setTimestamp(packet.getTimestamp())
                            .asPlayer(this.player);

                    write.message("/" + commandToRun);

                    return CompletableFuture.completedFuture(write.toServer());
                }

                return runCommand(this.server, this.player, commandToRun, hasRun -> {
                    if (!hasRun) {
                        return this.player.getChatBuilderFactory()
                                .builder()
                                .setTimestamp(packet.getTimestamp())
                                .asPlayer(this.player)
                                .message("/" + commandToRun)
                                .toServer();
                    }
                    return null;
                });
        }).thenApplyAsync(pkt -> pkt), Instant.now());
    }
}
