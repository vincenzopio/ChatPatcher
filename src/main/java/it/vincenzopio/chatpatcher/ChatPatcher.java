package it.vincenzopio.chatpatcher;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import it.vincenzopio.chatpatcher.server.DefaultPlayerListener;

import java.util.logging.LogManager;
import java.util.logging.Logger;


@Plugin(id = "chatpatcher", name = "Velocity Chat Patcher", version = "1.0.4", authors = "@vincenzopio")
public final class ChatPatcher {

    public static final Logger LOGGER = LogManager.getLogManager().getLogger("ChatPatcher");

    @Inject
    private ProxyServer proxyServer;


    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        LOGGER.info("Chat Patcher is now loading.");

        proxyServer.getEventManager().register(this, new DefaultPlayerListener(proxyServer));
    }

}
