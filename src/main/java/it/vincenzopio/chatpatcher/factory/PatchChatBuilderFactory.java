package it.vincenzopio.chatpatcher.factory;

import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderFactory;
import com.velocitypowered.proxy.protocol.packet.chat.builder.ChatBuilderV2;
import it.vincenzopio.chatpatcher.factory.builder.PatchChatBuilder;

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
