package dev.sergivos.core.messaging.brokers;

import dev.sergivos.core.messaging.MessagingService;
import org.checkerframework.checker.nullness.qual.NonNull;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RedisBroker extends MessagingBroker {
    private final @NonNull JedisPool pool;
    private final @NonNull ExecutorService executor;
    private final byte[] channelName;
    private final PubSub pubSub;
    private volatile boolean closed = false;

    public RedisBroker(final @NonNull MessagingService messagingService) {
        super(messagingService);
        this.channelName = messagingService.serviceName().getBytes(StandardCharsets.UTF_8);
        this.executor = Executors.newSingleThreadExecutor();

        final JedisPoolConfig config = new JedisPoolConfig();
        this.pool = new JedisPool(config, "127.0.0.1", 6379, 5000);
        this.pubSub = new PubSub();

        subscribe();
    }

    @Override
    public void close() {
        closed = true;
        executor.shutdownNow();
        pool.close();
    }

    @Override
    public void sendMessage(byte[] message) {
        try(final Jedis jedis = this.pool.getResource()) {
            jedis.publish(channelName, message);
        }
    }

    private void subscribe() {
        executor.execute(() -> {
            while(!closed) {
                try(final Jedis redis = this.pool.getResource()) {
                    redis.subscribe(this.pubSub, channelName);
                } catch(JedisException ex) {
                    if(!closed) {
                        System.err.println("Redis pub/sub disconnected. Reconnecting...");
                    }
                }
            }
        });
    }

    private class PubSub extends BinaryJedisPubSub {
        @Override
        public void onMessage(byte[] channel, byte[] message) {
            if(!Arrays.equals(channel, channelName)) return;

            messagingService.handleMessage(message);
        }
    }

}
