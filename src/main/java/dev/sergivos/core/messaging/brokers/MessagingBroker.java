package dev.sergivos.core.messaging.brokers;

import dev.sergivos.core.messaging.MessagingService;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class MessagingBroker {
    protected MessagingService messagingService;

    public MessagingBroker(final @NonNull MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    // TODO: change
    abstract public void close() throws Exception;

    // TODO: replace by Netty bytebuff
    abstract public void sendMessage(byte[] message);

}
