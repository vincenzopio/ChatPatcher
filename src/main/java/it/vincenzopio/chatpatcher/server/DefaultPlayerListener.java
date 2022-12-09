package it.vincenzopio.chatpatcher.server;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import it.vincenzopio.chatpatcher.factory.PatchChatBuilderFactory;
import it.vincenzopio.chatpatcher.server.chat.command.CPatchKeyedHandler;
import it.vincenzopio.chatpatcher.server.chat.command.CPatchSessionHandler;
import it.vincenzopio.chatpatcher.server.chat.message.MPatchKeyedHandler;
import it.vincenzopio.chatpatcher.server.chat.message.MPatchSessionHandler;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;

import static it.vincenzopio.chatpatcher.ChatPatcher.LOGGER;

public class DefaultPlayerListener {

    private final ProxyServer proxyServer;
    private final Set<Player> patchedPlayers = Collections.newSetFromMap(new WeakHashMap<>());

    public DefaultPlayerListener(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe(order = PostOrder.FIRST)
    public void serverPostConnectEvent(ServerPostConnectEvent event) {
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();

        // Skipping un-required players.
        if (player.getProtocolVersion().compareTo(ProtocolVersion.MINECRAFT_1_19) <= 0) {
            return;
        }

        if (patchedPlayers.contains(player)) {
            return;
        }

        patchedPlayers.add(player);

        LOGGER.info("Patching " + player.getUsername());

        VelocityServer velocityServer = (VelocityServer) proxyServer;
        ProtocolVersion protocolVersion = player.getProtocolVersion();

        try {
            // Patching out the player key!
            Field playerkey = player.getClass().getDeclaredField("playerKey");
            playerkey.setAccessible(true);
            playerkey.set(player, null);

            // Patching out the chat builder!
            Field chatBuilder = player.getClass().getDeclaredField("chatBuilderFactory");
            chatBuilder.setAccessible(true);
            chatBuilder.set(player, new PatchChatBuilderFactory(protocolVersion));

            // Need to patch the session handler >
            ClientPlaySessionHandler sessionHandler = (ClientPlaySessionHandler) player.getConnection().getSessionHandler();

            // Patching out the chat handler!
            Field chatHandler = sessionHandler.getClass().getDeclaredField("chatHandler");
            chatHandler.setAccessible(true);

            // Patching out the command handler!
            Field commandHandler = sessionHandler.getClass().getDeclaredField("commandHandler");
            commandHandler.setAccessible(true);

            // Chat & Commands for 1.19.3+
            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_19_3) >= 0) {
                chatHandler.set(sessionHandler, new MPatchSessionHandler(player, velocityServer));
                commandHandler.set(sessionHandler, new CPatchSessionHandler(player, velocityServer));
                return;
            }

            // Chat & Commands for 1.19
            chatHandler.set(sessionHandler, new MPatchKeyedHandler(velocityServer, player));
            commandHandler.set(sessionHandler, new CPatchKeyedHandler(player, velocityServer));
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "Could not patch player " + player.getUsername() + ". Have you updated to the latest build of Velocity (?)");
            patchedPlayers.remove(player);
        }

        LOGGER.info("Patched out " + player.getUsername() + " Replaced key: " + player.getIdentifiedKey() + " (should be null)");
    }

    @Subscribe
    public void playerDisconnectEvent(DisconnectEvent event) {
        patchedPlayers.remove(event.getPlayer());
    }
}
