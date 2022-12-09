package it.vincenzopio.chatpatcher.chat.command;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.crypto.IdentifiedKey;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerCommand;
import net.kyori.adventure.text.Component;

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
        queueCommandResult(this.server, this.player, event -> {
            CommandExecuteEvent.CommandResult result = event.getResult();
            IdentifiedKey playerKey = player.getIdentifiedKey();
            if (result == CommandExecuteEvent.CommandResult.denied()) {
                return CompletableFuture.completedFuture(null);
            }

            String commandToRun = result.getCommand().orElse(packet.getCommand());
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
                    if (commandToRun.equals(packet.getCommand())) {
                        return packet;
                    }


                    return this.player.getChatBuilderFactory()
                            .builder()
                            .setTimestamp(packet.getTimestamp())
                            .asPlayer(this.player)
                            .message("/" + commandToRun)
                            .toServer();
                }
                return null;
            });
        }, packet.getCommand(), packet.getTimestamp());
    }
}
