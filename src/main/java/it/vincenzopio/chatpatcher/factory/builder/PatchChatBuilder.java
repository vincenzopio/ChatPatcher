package it.vincenzopio.chatpatcher.factory.builder;

import com.google.common.collect.ImmutableList;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.ChatType;
import com.velocitypowered.proxy.protocol.packet.chat.ComponentHolder;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.SystemChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.keyed.KeyedPlayerCommandPacket;
import com.velocitypowered.proxy.protocol.packet.chat.legacy.LegacyChatPacket;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommandPacket;
import it.vincenzopio.chatpatcher.factory.mappings.MappedSessionChat;
import it.vincenzopio.chatpatcher.factory.mappings.MappedSessionCommand;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class PatchChatBuilder extends ChatBuilderV2 {

    public PatchChatBuilder(ProtocolVersion version) {
        super(version);
    }

    @Override
    public MinecraftPacket toClient() {
        if (version.compareTo(ProtocolVersion.MINECRAFT_1_19) >= 0) {
            //1.19+
            Component msg = component == null ? Component.text(message) : component;

            return new SystemChatPacket(new ComponentHolder(version, msg), type == ChatType.CHAT ? ChatType.SYSTEM : type);
        }

        //LEGACY
        UUID identity = sender == null ? (senderIdentity == null ? Identity.nil().uuid() : senderIdentity.uuid()) : sender.getUniqueId();
        Component msg = component == null ? Component.text(message) : component;

        return new LegacyChatPacket(ProtocolUtils.getJsonChatSerializer(version).serialize(msg), type.getId(), identity);
    }


    @Override
    public MinecraftPacket toServer() {
        this.timestamp = this.timestamp == null ? Instant.now(): this.timestamp;

        if (version.compareTo(ProtocolVersion.MINECRAFT_1_19_3) >= 0) {
            //1.19.3+
            if (message.startsWith("/")) {
                return new MappedSessionCommand(message.substring(1), timestamp, 0L, new SessionPlayerCommandPacket.ArgumentSignatures(), new LastSeenMessages())
                        .build(ProtocolUtils.Direction.SERVERBOUND, version);
            } else {
                return new MappedSessionChat(message, timestamp, 0L, false, new byte[0], new LastSeenMessages())
                        .build(ProtocolUtils.Direction.SERVERBOUND, version);
            }
        } else if (version.compareTo(ProtocolVersion.MINECRAFT_1_19) >= 0) {
            //1.19+
            if (message.startsWith("/")) {
                return new KeyedPlayerCommandPacket(message.substring(1), List.of(), timestamp);
            } else {
                var v1Chat = new KeyedPlayerChatPacket(message);
                v1Chat.setExpiry(this.timestamp);
                return v1Chat;
            }
        }

        //LEGACY
        var chat = new LegacyChatPacket();
        chat.setMessage(message);
        return chat;
    }
}
