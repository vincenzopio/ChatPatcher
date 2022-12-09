package it.vincenzopio.chatpatcher.builder.session;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChat;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PatchedProvider {
    private PatchedProvider() {}

    public static SessionPlayerChat chatProvider(MappedSessionChat map, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion){
        SessionPlayerChat sessionPlayerChat = new SessionPlayerChat();
        ByteBuf buf = Unpooled.buffer();

        ProtocolUtils.writeString(buf, map.getMessage());
        buf.writeLong(map.getTimestamp().toEpochMilli());
        buf.writeLong(map.getSalt());
        buf.writeBoolean(map.isSigned());
        if (map.isSigned()) {
            buf.writeBytes(map.getSignature());
        }

        map.getLastSeenMessages().encode(buf);

        sessionPlayerChat.decode(buf, direction, protocolVersion);

        return sessionPlayerChat;
    }

    public static SessionPlayerCommand commandProvider(MappedSessionCommand map, ProtocolUtils.Direction direction, ProtocolVersion protocolVersion){
        SessionPlayerCommand command = new SessionPlayerCommand();
        ByteBuf buf = Unpooled.buffer();

        ProtocolUtils.writeString(buf, map.getCommand());
        buf.writeLong(map.getTimeStamp().toEpochMilli());
        buf.writeLong(map.getSalt());
        map.getArgumentSignatures().encode(buf);
        map.getLastSeenMessages().encode(buf);

        command.decode(buf, direction, protocolVersion);

        return command;
    }
}
