package it.vincenzopio.chatpatcher.builder.session;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChat;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;

import java.time.Instant;

public class MappedSessionChat extends SessionPlayerChat {

    public MappedSessionChat() {}

    public MappedSessionChat(String message, Instant timestamp, long salt, boolean signed, byte[] signature, LastSeenMessages lastSeenMessages) {
        this.message = message;
        this.timestamp = timestamp;
        this.salt = salt;
        this.signed = signed;
        this.signature = signature;
        this.lastSeenMessages = lastSeenMessages;
    }

    public SessionPlayerChat build(ProtocolUtils.Direction direction, ProtocolVersion protocolVersion){
        return PatchedProvider.chatProvider(this, direction, protocolVersion);
    }
}
