package dev.sergivos.messaging.brokers;

import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import io.nats.client.Options;
import java.io.IOException;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class NatsBroker extends MessagingBroker {

  private final @NonNull String channelName;
  private final @NonNull Connection connection;
  private final @NonNull Dispatcher dispatcher;

  public NatsBroker(final @NonNull String url) throws IOException, InterruptedException {
    this.channelName = messagingService.serviceName();

    final Options options = new Options.Builder()
        .server(url)
        .connectionName("MS-" + this.channelName)
        .connectionListener((conn, type) -> messagingService.logger().info(type.toString()))
        .build();

    this.connection = Nats.connect(options);
    this.dispatcher = connection.createDispatcher();

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
    dispatcher.subscribe(channelName, message -> messagingService.handleMessage(message.getData()));
  }

}
