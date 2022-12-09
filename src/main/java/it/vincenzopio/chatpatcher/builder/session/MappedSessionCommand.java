package it.vincenzopio.chatpatcher.builder.session;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.ProtocolUtils;
import com.velocitypowered.proxy.protocol.packet.chat.LastSeenMessages;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerCommand;

import java.time.Instant;

public class MappedSessionCommand extends SessionPlayerCommand {
    public MappedSessionCommand(String command, Instant timeStamp, long salt, ArgumentSignatures argumentSignatures, LastSeenMessages lastSeenMessages) {
        this.command = command;
        this.timeStamp = timeStamp;
        this.salt = salt;
        this.argumentSignatures = argumentSignatures;
        this.lastSeenMessages = lastSeenMessages;
    }

    public long getSalt(){
        return salt;
    }

    public ArgumentSignatures getArgumentSignatures(){
        return argumentSignatures;
    }
    public LastSeenMessages getLastSeenMessages(){
        return lastSeenMessages;
    }


    public SessionPlayerCommand build(ProtocolUtils.Direction direction, ProtocolVersion protocolVersion){
        return PatchedProvider.commandProvider(this, direction, protocolVersion);
    }

}
