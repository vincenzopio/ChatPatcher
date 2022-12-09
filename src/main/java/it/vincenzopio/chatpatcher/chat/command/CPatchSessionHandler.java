package it.vincenzopio.chatpatcher.chat.command;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;

import java.util.concurrent.CompletableFuture;

public class CPatchSessionHandler implements CommandHandler<SessionPlayerCommand> {
    private final ConnectedPlayer player;
    private final VelocityServer server;

    public CPatchSessionHandler(ConnectedPlayer player, VelocityServer server) {
        this.player = player;
        this.server = server;
    }

    @Override
    public Class<SessionPlayerCommand> packetClass() {
        return SessionPlayerCommand.class;
    }

    @Override
    public void handlePlayerCommandInternal(SessionPlayerCommand packet) {
        queueCommandResult(this.server, this.player, event -> {
            CommandExecuteEvent.CommandResult result = event.getResult();
            if (result == CommandExecuteEvent.CommandResult.denied()) {
                return CompletableFuture.completedFuture(null);
            }

            String commandToRun = result.getCommand().orElse(packet.getCommand());
            if (result.isForwardToServer()) {
                if (commandToRun.equals(packet.getCommand())) {
                    return CompletableFuture.completedFuture(packet);
                } else {
                    return CompletableFuture.completedFuture(this.player.getChatBuilderFactory()
                            .builder()
                            .setTimestamp(packet.getTimeStamp())
                            .asPlayer(this.player)
                            .message("/" + commandToRun)
                            .toServer());
                }
            }

            return runCommand(this.server, this.player, commandToRun, hasRun -> {
                if (!hasRun) {
                    return this.player.getChatBuilderFactory()
                            .builder()
                            .setTimestamp(packet.getTimeStamp())
                            .asPlayer(this.player)
                            .message("/" + commandToRun)
                            .toServer();

                }
                return null;
            });
        }, packet.getCommand(), packet.getTimeStamp());
    }
}
