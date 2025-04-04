package it.vincenzopio.chatpatcher.factory.mappings;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChatPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.time.Instant;

public final class MappedSessionChat {

    private final String message;
    private final Instant timestamp;
    private final long salt;
    private final boolean signed;
    private final byte[] signature;
    private final LastSeenMessages lastSeenMessages;

    public MappedSessionChat(String message, Instant timestamp, long salt, boolean signed, byte[] signature, LastSeenMessages lastSeenMessages) {
        this.message = message;
        this.timestamp = timestamp;
        this.salt = salt;
        this.signed = signed;
        this.signature = signature;
        this.lastSeenMessages = lastSeenMessages;
    }

    public SessionPlayerChatPacket build(ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        var sessionPlayerChat = new SessionPlayerChatPacket();
        ByteBuf buf = Unpooled.buffer();

        ProtocolUtils.writeString(buf, message);
        buf.writeLong(timestamp.toEpochMilli());
        buf.writeLong(salt);
        buf.writeBoolean(signed);
        if (signed) {
            buf.writeBytes(signature);
        }

        lastSeenMessages.encode(buf, protocolVersion);

        sessionPlayerChat.decode(buf, direction, protocolVersion);

        return sessionPlayerChat;
    }
}
