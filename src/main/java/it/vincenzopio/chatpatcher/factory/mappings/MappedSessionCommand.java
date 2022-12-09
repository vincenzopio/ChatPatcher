package it.vincenzopio.chatpatcher.factory.mappings;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.time.Instant;

public class MappedSessionCommand {

    private final String command;
    private final Instant timeStamp;
    private final long salt;
    private final SessionPlayerCommand.ArgumentSignatures argumentSignatures;
    private final LastSeenMessages lastSeenMessages;

    public MappedSessionCommand(String command, Instant timeStamp, long salt, SessionPlayerCommand.ArgumentSignatures argumentSignatures, LastSeenMessages lastSeenMessages) {
        this.command = command;
        this.timeStamp = timeStamp;
        this.salt = salt;
        this.argumentSignatures = argumentSignatures;
        this.lastSeenMessages = lastSeenMessages;
    }

    public SessionPlayerCommand build(ProtocolUtils.Direction direction, ProtocolVersion protocolVersion) {
        SessionPlayerCommand sessionPlayerCommand = new SessionPlayerCommand();
        ByteBuf buf = Unpooled.buffer();

        ProtocolUtils.writeString(buf, command);
        buf.writeLong(timeStamp.toEpochMilli());
        buf.writeLong(salt);
        argumentSignatures.encode(buf);
        lastSeenMessages.encode(buf);

        sessionPlayerCommand.decode(buf, direction, protocolVersion);

        return sessionPlayerCommand;
    }

}