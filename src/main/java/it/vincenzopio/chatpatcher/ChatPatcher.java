package it.vincenzopio.chatpatcher;

import com.google.inject.Inject;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.proxy.VelocityServer;
import com.velocitypowered.proxy.connection.client.ClientPlaySessionHandler;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import com.velocitypowered.proxy.protocol.packet.chat.legacy.LegacyChatHandler;
import it.vincenzopio.chatpatcher.builder.PatchChatBuilderFactory;
import it.vincenzopio.chatpatcher.chat.command.CPatchKeyedHandler;
import it.vincenzopio.chatpatcher.chat.command.CPatchSessionHandler;
import it.vincenzopio.chatpatcher.chat.message.MPatchKeyedHandler;
import it.vincenzopio.chatpatcher.chat.message.MPatchSessionHandler;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


@Plugin(id = "chatpatcher", name = "Velocity Chat Patcher", version = "1.0.0", authors = "@vincenzopio")
public class ChatPatcher {

    private final List<Player> patchedPlayers = new ArrayList<>();

    @Inject
    private ProxyServer proxyServer;

    @Inject
    private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Chat Patcher is now loading.");

    }

    @Subscribe(order = PostOrder.FIRST)
    public void playerJoinEvent(ServerPostConnectEvent event) {
        ConnectedPlayer player = (ConnectedPlayer) event.getPlayer();

        if (patchedPlayers.contains(player)) {
            return;
        }

        patchedPlayers.add(player);

        logger.info("Patching out " + player.getUsername());

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

            if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_19_3) >= 0) {
                chatHandler.set(sessionHandler, new MPatchSessionHandler(player, velocityServer));
                commandHandler.set(sessionHandler, new CPatchSessionHandler(player, velocityServer));
            } else if (protocolVersion.compareTo(ProtocolVersion.MINECRAFT_1_19) >= 0) {
                chatHandler.set(sessionHandler, new MPatchKeyedHandler(velocityServer, player));
                commandHandler.set(sessionHandler, new CPatchKeyedHandler(player, velocityServer));
            } else {
                chatHandler.set(sessionHandler, new LegacyChatHandler(velocityServer, player));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("Patched out " + player.getUsername() + " key: " + player.getIdentifiedKey());


    }
}
