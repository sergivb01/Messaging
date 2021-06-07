package dev.sergivos.core.messaging.brokers;

import dev.sergivos.core.messaging.MessagingService;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class NatsBroker extends MessagingBroker {
    private final String channelName;
    private final Connection connection;
    private @MonotonicNonNull Dispatcher dispatcher;

    public NatsBroker(final @NonNull MessagingService messagingService, final @NonNull String url) throws IOException, InterruptedException {
        super(messagingService);
        this.channelName = messagingService.serviceName();

        final Options options = new Options.Builder()
                .server(url)
                .connectionName("MS-" + this.channelName)
                .connectionListener((conn, type) -> messagingService.logger().info(type.toString()))
                .build();
        this.connection = Nats.connect(options);
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
