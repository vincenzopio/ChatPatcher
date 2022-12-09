package it.vincenzopio.chatpatcher.builder;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderFactory;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import com.velocitypowered.proxy.protocol.packet.chat.session.SessionPlayerChat;
import it.vincenzopio.chatpatcher.builder.v2.PatchChatBuilder;

import java.util.function.Function;

public class PatchChatBuilderFactory extends ChatBuilderFactory {

    private final ProtocolVersion protocolVersion;
    private final Function<ProtocolVersion, ChatBuilderV2> builderFunction;
    public PatchChatBuilderFactory(ProtocolVersion version) {
        super(version);
        this.protocolVersion = version;

        this.builderFunction = PatchChatBuilder::new;
    }

    @Override
    public ChatBuilderV2 builder() {
        return this.builderFunction.apply(protocolVersion);
    }




}
