package it.vincenzopio.chatpatcher.server;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import it.vincenzopio.chatpatcher.factory.PatchChatBuilderFactory;
import it.vincenzopio.chatpatcher.server.chat.command.CPatchKeyedHandler;
import it.vincenzopio.chatpatcher.server.chat.command.CPatchSessionHandler;
import it.vincenzopio.chatpatcher.server.chat.message.MPatchKeyedHandler;
import it.vincenzopio.chatpatcher.server.chat.message.MPatchSessionHandler;

import java.util.logging.Level;

import static it.vincenzopio.chatpatcher.ChatPatcher.LOGGER;
import static it.vincenzopio.chatpatcher.utils.ReflectionUtils.setField;

public final class DefaultPlayerListener {

    private final ProxyServer proxyServer;

    public DefaultPlayerListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void serverConnectedEvent(ServerConnectedEvent event) {
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();

        // Skipping not required players.
        if (player.getProtocolVersion().getProtocol() < 759) {
            return;
        }

        VelocityServer velocityServer = (VelocityServer) proxyServer;
        ProtocolVersion protocolVersion = player.getProtocolVersion();

        try {
            var sessionHandler = player.getConnection().getActiveSessionHandler();

            if (!(sessionHandler instanceof ClientPlaySessionHandler)) {
                LOGGER.warning("Could not patch player " + player.getUsername() + ". Session handler is not a ClientPlaySessionHandler (%s)".formatted(sessionHandler.getClass().getSimpleName()));
                return;
            }


            // Removing the player key
            setField(player, "playerKey", null);

            // Replace the chat builder factory with our patched one
            setField(player, "chatBuilderFactory", new PatchChatBuilderFactory(protocolVersion));

            // Chat & Commands for 1.19.3+
            if (protocolVersion.noLessThan(ProtocolVersion.MINECRAFT_1_19_3)) {
                LOGGER.info(() -> "Session Patching " + player.getUsername() + " Protocol: " + protocolVersion);

                setField(sessionHandler, "chatHandler", new MPatchSessionHandler(player, velocityServer));
                setField(sessionHandler, "commandHandler", new CPatchSessionHandler(player, velocityServer));
                return;
            }

            LOGGER.info(() -> "Keyed Patching " + player.getUsername() + " Protocol: " + protocolVersion);

            // Chat & Commands for 1.19
            setField(sessionHandler, "chatHandler", new MPatchKeyedHandler(player, velocityServer));
            setField(sessionHandler, "commandHandler", new CPatchKeyedHandler(player, velocityServer));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Could not patch player " + player.getUsername() + ". Have you updated to the latest build of Velocity (?)");
        }
    }

}
