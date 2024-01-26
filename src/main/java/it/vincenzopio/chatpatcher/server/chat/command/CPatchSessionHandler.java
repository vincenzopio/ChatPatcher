package it.vincenzopio.chatpatcher.server.chat.command;

import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.CommandHandler;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket;

import java.util.concurrent.CompletableFuture;

public class CPatchSessionHandler implements CommandHandler<SessionPlayerCommandPacket> {
    private final ConnectedPlayer player;
    private final VelocityServer server;

    public CPatchSessionHandler(ConnectedPlayer player, VelocityServer server) {
        this.player = player;
        this.server = server;
    }

    @Override
    public Class<SessionPlayerCommandPacket> packetClass() {
        return SessionPlayerCommandPacket.class;
    }


    @Override
    public void handlePlayerCommandInternal(SessionPlayerCommandPacket packet) {
        queueCommandResult(this.server, this.player, event -> {
            CommandExecuteEvent.CommandResult result = event.getResult();

            if (result == CommandExecuteEvent.CommandResult.denied()) {
                return CompletableFuture.completedFuture(null);
            }

            String commandToRun = result.getCommand().orElse(packet.getCommand());
            if (result.isForwardToServer()) {
                ChatBuilderV2 write = this.player.getChatBuilderFactory()
                        .builder()
                        .setTimestamp(packet.getTimeStamp())
                        .asPlayer(this.player)
                        .message("/" + commandToRun);

                return CompletableFuture.completedFuture(write.toServer());
            }

            return runCommand(this.server, this.player, commandToRun, hasRun -> {
                if (hasRun) return null;

                return this.player.getChatBuilderFactory()
                        .builder()
                        .setTimestamp(packet.getTimeStamp())
                        .asPlayer(this.player)
                        .message("/" + commandToRun)
                        .toServer();

            });
        }, packet.getCommand(), packet.getTimeStamp());
    }
}
