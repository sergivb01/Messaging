package dev.sergivos.core.messaging;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.sergivos.core.Core;
import dev.sergivos.core.messaging.brokers.MessagingBroker;
import dev.sergivos.core.messaging.brokers.NatsBroker;
import dev.sergivos.core.messaging.compression.Compression;
import dev.sergivos.core.messaging.compression.NoCompression;
import dev.sergivos.core.messaging.packets.Packet;
import dev.sergivos.core.messaging.packets.PacketManager;
import dev.sergivos.core.messaging.packets.PacketUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static dev.sergivos.core.utils.MathUtil.percentile;

/**
 * This service provides the ability to send and handle {@link Packet}s across multiple
 * services or Minecraft Servers. It is intended to be detached from any platform, so as to
 * be able to use it in {@code Velocity}, {@code Bukkit} or even {@code MineStorm}.
 *
 * <h3>Packet registration</h3>
 * In order to send packets, you must register the Packet classes to a proper {@link PacketManager}. This will
 * translate the different {@link Packet}s objects into proper IDs that we can send across the systems.
 *
 * <h3>Packet format</h3>
 * <pre>
 *      +--------------------+-------------------+---------------+-------------+
 *      | Server ID (String) | Message ID (UUID) | Type (String) | Packet Data |
 *      +--------------------+-------------------+---------------+-------------+
 * </pre>
 */

// TODO: optimization by writing/reading serverId un-compressed, we can check if we've sent this packet before de-compressing
public class MessagingService {
    private final ByteBufAllocator bufferPool = PooledByteBufAllocator.DEFAULT;
    private final int[] capacities = new int[150];
    private final AtomicInteger currentCapacity = new AtomicInteger(0);
    private final ReadWriteLock capacityLock = new ReentrantReadWriteLock();
    private final Compression compression = new NoCompression();

    private final @NonNull PacketManager packetManager;
    private final @NonNull UUID serverId;
    private final @NonNull MessagingBroker broker;
    private final @NonNull ExecutorService executor;
    private final ReadWriteLock shutdownLock = new ReentrantReadWriteLock();

    private final @NonNull Logger logger;

    private volatile int capacity = 2 * 1024; // Start at 2kb

    /**
     * Creates a new manager with the established data
     *
     * @param packetManager The manager that will handle packet translation IDs and classes
     * @param logger        The logger to log errors and information
     */
    public MessagingService(@NonNull final PacketManager packetManager, @NonNull final Logger logger) throws IOException, InterruptedException {
        this.packetManager = packetManager;
        this.serverId = UUID.randomUUID();
        this.logger = logger;

        this.executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("MessagingService-%d").build());

        this.broker = new NatsBroker(this, "test", "nats://127.0.0.1:4222,nats://127.0.0.1:5222,nats://127.0.0.1:6222");

        logger.info("Created MessagingService with {} using id {} and {} registered packets", this.broker.getClass().getSimpleName(), this.serverId, this.packetManager.numerRegisteredPackets());
    }

    /**
     * Closes the messaging system
     */
    public void close() {
        shutdownLock.writeLock().lock();
        logger.info("Shutting down MessagingService");

        executor.shutdown();
        try {
            if(!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch(InterruptedException ex) {
            logger.error("error shutting down executor from MessagingService", ex);
        }

        try {
            broker.close();
        } catch(Exception ex) {
            logger.error("error shutting down broker", ex);
        } finally {
            shutdownLock.writeLock().unlock();
        }
    }

    /**
     * Sends a packet through the system through a {@link ExecutorService}.
     * If there's an exception during sending, it won't be able to handled it properly
     *
     * @param packet The {@link Packet} to be sent
     * @throws IllegalStateException if the packet is not registered in the {@link PacketManager}
     */
    public void sendPacket(@NonNull final Packet packet, boolean async) throws IllegalStateException {
        shutdownLock.readLock().lock();
        try {
            final String packetType = packetManager.id(packet);
            if(packetType == null) {
                throw new IllegalStateException("Packet " + packet.getClass().getSimpleName() + " is not registered!");
            }

            final Runnable runnable = () -> {
                final ByteBuf buf = bufferPool.buffer(getInitialCapacity());
                try {
                    // write serverId and packetId
                    PacketUtils.writeUuid(buf, this.serverId);
                    PacketUtils.writeUuid(buf, UUID.randomUUID());

                    // write packetType and the actual packet
                    PacketUtils.writeString(buf, packetType);
                    packet.write(buf);

                    final byte[] data = compression.compress(buf);
                    try {
                        broker.sendMessage(data);
                    } catch(Exception ex) {
                        logger.error("error sending packet {} to broker: {}", packet, ex);
                    }
                } finally {
                    addCapacity(buf.writerIndex());
                }
            };

            if(async) {
                executor.execute(runnable);
            } else {
                runnable.run();
            }
        } finally {
            shutdownLock.readLock().unlock();
        }
    }

    /**
     * Decodes and handles the {@link Packet}
     *
     * @param message The raw bytes of a {@link Packet} read from a {@link MessagingBroker}
     */
    public void handleMessage(byte[] message) {
        // we run this code in another thread so we can start processing another packet ASAP
        // while we're decompressing + deserializing + handling the other packet
        final ByteBuf buf = compression.decompress(message);
        try {
            final UUID sender = PacketUtils.readUuid(buf);
            // TODO: comment for dev
            if(sender.equals(serverId)) {
                // we've sent this packet, no need to handle it
                return;
            }
            final UUID id = PacketUtils.readUuid(buf);

            final String packetType = PacketUtils.readString(buf);
            final Packet packet = packetManager.read(packetType, buf);
            if(packet == null) {
                // TODO: we received an unknown packet, should we handle it properly or send an exception?
                logger.warn("Received an unknown packet from {} (PacketType={})", sender, packetType);
                return;
            }

            // TODO: migrate to platform independent solution
            Bukkit.getScheduler().runTask(Core.INSTANCE, packet::callEvent);
        } catch(Exception ex) {
            logger.error("error handling packet", ex);
        } finally {
            buf.release();
        }
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
