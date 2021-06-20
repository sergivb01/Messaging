package dev.sergivos.core.network;

import dev.sergivos.messaging.packets.Packet;
import dev.sergivos.messaging.packets.PacketUtils;
import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimplePacket implements Packet {
    private @MonotonicNonNull String sender;
    private @MonotonicNonNull Component message;

    public SimplePacket() {
    }

    public SimplePacket(final @NonNull String sender, final @NonNull Component message) {
        this.sender = sender;
        this.message = message;
    }

    public @MonotonicNonNull String sender() {
        return this.sender;
    }

    public @MonotonicNonNull Component message() {
        return this.message;
    }

    @Override
    public void read(final @NonNull ByteBuf buf) {
        this.sender = PacketUtils.readString(buf);
        this.message = PacketUtils.readComponent(buf);
    }

    @Override
    public void write(final @NonNull ByteBuf buf) {
        PacketUtils.writeString(buf, this.sender);
        PacketUtils.writeComponent(buf, this.message);
    }

    @Override
    public String toString() {
        return "SimplePacket{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
