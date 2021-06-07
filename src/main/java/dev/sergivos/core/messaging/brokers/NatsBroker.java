package dev.sergivos.core.messaging.brokers;

import dev.sergivos.core.messaging.MessagingService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NatsBroker extends MessagingBroker {
    private final ReadWriteLock queueLock = new ReentrantReadWriteLock();
    private final String channelName;
    private final Connection connection;
    private @MonotonicNonNull Dispatcher dispatcher;

    public NatsBroker(final @NonNull MessagingService messagingService, final @NonNull String channelName,
                      final @NonNull String url) throws IOException, InterruptedException {
        super(messagingService);
        this.channelName = channelName;

        this.connection = Nats.connect(url);
        subscribe();
    }

    @Override
    public void close() throws Exception {
        connection.closeDispatcher(dispatcher);
        connection.close();
    }

    @Override
    public void sendMessage(byte[] message) {
        connection.publish(channelName, message);
    }

    private void subscribe() {
        dispatcher = connection.createDispatcher(message -> messagingService.handleMessage(message.getData()))
                .subscribe(channelName);
    }

}
