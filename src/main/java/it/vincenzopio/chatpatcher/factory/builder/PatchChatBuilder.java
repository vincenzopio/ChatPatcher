package it.vincenzopio.chatpatcher.factory.builder;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ChatType;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.SystemChat;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChat;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerCommand;
import com.velocitypowered.proxy.protocol.packet.chat.legacy.LegacyChat;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;
import it.vincenzopio.chatpatcher.factory.mappings.MappedSessionChat;
import it.vincenzopio.chatpatcher.factory.mappings.MappedSessionCommand;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.UUID;

public class PatchChatBuilder extends ChatBuilderV2 {

    public PatchChatBuilder(ProtocolVersion version) {
        super(version);
    }

    @Override
    public MinecraftPacket toClient() {
        if (version.compareTo(ProtocolVersion.MINECRAFT_1_19_1) >= 0) {
            //1.19+
            Component msg = component == null ? Component.text(message) : component;
            return new SystemChat(msg, type == ChatType.CHAT ? ChatType.SYSTEM : type);
        }

        //LEGACY
        UUID identity = sender == null ? (senderIdentity == null ? Identity.nil().uuid() : senderIdentity.uuid()) : sender.getUniqueId();
        Component msg = component == null ? Component.text(message) : component;

        return new LegacyChat(ProtocolUtils.getJsonChatSerializer(version).serialize(msg), type.getId(), identity);
    }


    @Override
    public MinecraftPacket toServer() {
        if (version.compareTo(ProtocolVersion.MINECRAFT_1_19_3) >= 0) {
            //1.19.3+
            if (message.startsWith("/")) {
                return new MappedSessionCommand(message.substring(1), timestamp, 0L, new SessionPlayerCommand.ArgumentSignatures(), new LastSeenMessages())
                        .build(ProtocolUtils.Direction.SERVERBOUND, version);
            } else {
                return new MappedSessionChat(message, timestamp, 0L, false, new byte[0], new LastSeenMessages())
                        .build(ProtocolUtils.Direction.SERVERBOUND, version);
            }
        } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_19_1) >= 0) {
            //1.19+
            if (message.startsWith("/")) {
                return new KeyedPlayerCommand(message.substring(1), ImmutableList.of(), timestamp);
            } else {
                KeyedPlayerChat v1Chat = new KeyedPlayerChat(message);
                v1Chat.setExpiry(this.timestamp);
                return v1Chat;
            }
        }
        //LEGACY
        LegacyChat chat = new LegacyChat();
        chat.setMessage(message);
        return chat;
    }


}
