package dev.sergivos.core.network;

import dev.sergivos.messaging.packets.Packet;
import dev.sergivos.messaging.packets.PacketUtils;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimplePacket implements Packet {
    private @MonotonicNonNull String sender;
    private @MonotonicNonNull String message;

    public SimplePacket() {
    }

    public SimplePacket(final @NonNull String sender, final @NonNull String message) {
        this.sender = sender;
        this.message = message;
    }

    public String sender() {
        return this.sender;
    }

    public String message() {
        return this.message;
    }

    @Override
    public void read(@NonNull ByteBuf buf) {
        this.sender = PacketUtils.readString(buf);
        this.message = PacketUtils.readString(buf);
    }

    @Override
    public void write(@NonNull ByteBuf buf) {
        PacketUtils.writeString(buf, this.sender);
        PacketUtils.writeString(buf, this.message);
    }

    @Override
    public String toString() {
        return "SimplePacket{" +
                "sender='" + sender + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
