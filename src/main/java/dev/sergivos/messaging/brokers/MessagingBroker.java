package dev.sergivos.messaging.brokers;

import dev.sergivos.messaging.MessagingService;
import org.checkerframework.checker.nullness.qual.NonNull;

public abstract class MessagingBroker {

  protected final @NonNull MessagingService messagingService;

  /**
   * Creates a messaging broker used by the {@link MessagingService}
   *
   * @param messagingService The service that will use this broker
   */
  public MessagingBroker(final @NonNull MessagingService messagingService) {
    this.messagingService = messagingService;
  }

  /**
   * Closes the Broker. The {@link MessagingService} will close the services so there's no need to
   * call this yourself. Please call {@link MessagingService#close()} instead
   *
   * @throws Exception if there was an error during shutdown
   */
  abstract public void close() throws Exception;

  /**
   * Sends a message though the broker. Used by a {@link MessagingService}
   *
   * @param message The message to be sent
   */
  abstract public void sendMessage(byte[] message);

}
