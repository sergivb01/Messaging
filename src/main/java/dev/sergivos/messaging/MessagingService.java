package dev.sergivos.messaging;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.sergivos.messaging.brokers.MessagingBroker;
import dev.sergivos.messaging.brokers.NatsBroker;
import dev.sergivos.messaging.compression.MessagingCompression;
import dev.sergivos.messaging.compression.NoCompression;
import dev.sergivos.messaging.packets.Packet;
import dev.sergivos.messaging.packets.PacketManager;
import dev.sergivos.messaging.packets.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static dev.sergivos.messaging.utils.MathUtil.percentile;

/**
 * <p>
 * This service provides the ability to send and handle {@link Packet}s across multiple
 * services or Minecraft Servers. It is intended to be detached from any platform, so as to
 * be able to use it in {@code Velocity}, {@code Bukkit} or even {@code MineStorm}.
 * </p><br>
 *
 * <strong>Packet registration</strong>
 * <p>
 * In order to send packets, you must register the Packet classes to a proper {@link PacketManager}. This will
 * translate the different {@link Packet}s objects into proper IDs that we can send across the systems.
 * </p><br>
 *
 * <strong>Packet format</strong>
 * <pre>
 *      +--------------------+-------------------+---------------+-------------+
 *      | Server ID (String) | Message ID (UUID) | Type (String) | Packet Data |
 *      +--------------------+-------------------+---------------+-------------+
 * </pre>
 */
@SuppressWarnings("UnstableApiUsage")
public final class MessagingService {
    private final ByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;
    private final int[] capacities = new int[150];
    private final AtomicInteger currentCapacity = new AtomicInteger(0);
    private final ReadWriteLock capacityLock = new ReentrantReadWriteLock();

    private final @NonNull MessagingCompression compression = new NoCompression();
    private final @NonNull PacketManager packetManager;
    private final @NonNull UUID serverId;
    private final @NonNull String serviceName;
    private final @NonNull MessagingBroker broker;
    private final @NonNull ExecutorService executorService;
    private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

    private final @NonNull EventBus eventBus;
    private final @NonNull Logger logger;

    private volatile int capacity = 2 * 1024; // Start at 2kb

    /**
     * Creates a new manager with the established data.
     * The {@code serviceName} will be normalized
     *
     * @param serviceName   The service's name. Will be used as a channel name on the broker, you will need
     *                      a matching {@code serviceName} {@link MessagingService} on another instance.
     * @param packetManager The manager that will handle packet translation IDs and classes
     * @throws IOException          if the broker failed to initialize
     * @throws InterruptedException if the broker failed to initialize
     */
    public MessagingService(final @NonNull String serviceName, final @NonNull PacketManager packetManager) throws IOException, InterruptedException {
        this.packetManager = packetManager;
        this.serverId = UUID.randomUUID();
        this.serviceName = serviceName.trim().replace(" ", "_");
        this.eventBus = new EventBus(serviceName);
        this.logger = LoggerFactory.getLogger(this.serviceName);

        this.executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MessagingService-%d").build());

        // TODO: move to constructor, but we'll need to refactor as the MessageBroker depends on a MessagingService for the channel-name
        this.broker = new NatsBroker(this, "nats://127.0.0.1:4222,nats://127.0.0.1:5222,nats://127.0.0.1:6222");

        logger.info("MessagingService {} created: using broker {} and id {}", serviceName, this.broker.getClass().getSimpleName(), this.serverId);
    }

    /**
     * Closes the messaging system
     */
    public void close() {
        shutdownLock.writeLock().lock();
        logger.info("Shutting down MessagingService");

        try {
            executorService.shutdown();
            if(!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }

            try {
                broker.close();
            } catch(Exception ex) {
                logger.error("error shutting down broker", ex);
            }
        } catch(InterruptedException ex) {
            logger.error("error shutting down executor from MessagingService", ex);
        } finally {
            shutdownLock.writeLock().unlock();
            logger.info("MessagingService shutdown.");
        }
    }

    /**
     * Registers a listener for {@link Packet}s
     *
     * @param object The instance of the {@link Packet} listener.
     */
    public void registerListener(final @NonNull Object object) {
        eventBus.register(object);
    }

    /**
     * Un-registers a listener for {@link Packet}s
     *
     * @param object The instance of the {@link Packet} listener.
     */
    public void unregisterListener(final @NonNull Object object) {
        eventBus.unregister(object);
    }

    /**
     * Sends a packet <strong>asynchronously</strong>.
     * See also {@link MessagingService#sendPacket(Packet, boolean)}
     *
     * @param packet The {@link Packet} to be sent
     * @throws NullPointerException if the packet is not registered in the {@link PacketManager}
     */
    public void sendPacket(final @NonNull Packet packet) throws NullPointerException {
        sendPacket(packet, true);
    }

    /**
     * Sends a packet <strong>synchronously/asynchronously</strong> depending on the {@code async} param
     * If there's an exception during sending, it won't be able to handled it properly
     *
     * @param packet The {@link Packet} to be sent
     * @param async  Whether the {@link Packet} should be sent asynchronously or not
     * @throws NullPointerException if the packet is not registered in the {@link PacketManager}
     */
    public void sendPacket(final @NonNull Packet packet, boolean async) throws NullPointerException {
        shutdownLock.readLock().lock();
        final Executor executor = async ? executorService : MoreExecutors.directExecutor();
        try {
            final String packetType = packetManager.id(packet);
            if(packetType == null) {
                throw new IllegalStateException("Packet " + packet.getClass().getSimpleName() + " is not registered!");
            }

            executor.execute(() -> {
                final ByteBuf buf = ALLOCATOR.buffer(getInitialCapacity());
                try {
                    // write serverId and packetId
                    PacketUtils.writeUuid(buf, this.serverId);

                    // write packetType and the actual packet
                    PacketUtils.writeString(buf, packetType);
                    packet.write(buf);

                    final byte[] data = compression.compress(buf);
                    broker.sendMessage(data);
                } catch(Exception ex) {
                    logger.error("error sending packet " + packet + " to broker", ex);
                } finally {
                    addCapacity(buf.writerIndex());
                    buf.release();
                }
            });
        } finally {
            shutdownLock.readLock().unlock();
        }
    }

    /**
     * Decodes and handles the {@link Packet}
     *
     * @param message The raw bytes of a {@link Packet} newInstance from a {@link MessagingBroker}
     */
    public void handleMessage(byte[] message) {
        final ByteBuf buf = compression.decompress(message);
        try {
            final UUID sender = PacketUtils.readUuid(buf);
            // TODO: comment for dev
//            if(sender.equals(serverId)) {
//                // we've sent this packet, no need to handle it
//                return;
//            }

            final String packetType = PacketUtils.readString(buf);
            final Packet packet = packetManager.newInstance(packetType);
            if(packet == null) {
                // TODO: we received an unknown packet, should we handle it properly or send an exception?
                logger.warn("Received an unknown packet from {} (PacketType={})", sender, packetType);
                return;
            }
            packet.read(buf);

            eventBus.post(packet);
        } catch(Exception ex) {
            logger.error("error handling packet", ex);
        } finally {
            buf.release();
        }
    }

    public @NonNull Logger logger() {
        return this.logger;
    }

    public @NonNull PacketManager packetManager() {
        return this.packetManager;
    }

    public @NonNull UUID serverId() {
        return this.serverId;
    }

    public @NonNull String serviceName() {
        return this.serviceName;
    }

    private int getInitialCapacity() {
        capacityLock.readLock().lock();
        try {
            return capacity;
        } finally {
            capacityLock.readLock().unlock();
        }
    }

    private void addCapacity(final int newCapacity) {
        int current = currentCapacity.getAndIncrement();
        if(current < 150) {
            capacities[current] = newCapacity;
        } else {
            capacityLock.writeLock().lock();
            try {
                capacity = percentile(capacities, 80);
                currentCapacity.set(0);
            } finally {
                capacityLock.writeLock().unlock();
            }
        }
    }

}
