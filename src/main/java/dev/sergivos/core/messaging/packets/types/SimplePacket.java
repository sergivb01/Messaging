package dev.sergivos.core.messaging.packets.types;

import dev.sergivos.core.messaging.packets.Packet;
import dev.sergivos.core.messaging.packets.PacketUtils;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SimplePacket extends Packet {
    private String test;

    public SimplePacket() {
    }

    public SimplePacket(String a) {
        this.test = a;
    }

    @Override
    public void read(@NonNull ByteBuf buf) {
        this.test = PacketUtils.readString(buf);
    }

    @Override
    public void write(@NonNull ByteBuf buf) {
        PacketUtils.writeString(buf, this.test);
    }

    @Override
    public String toString() {
        return "SimplePacket{" +
                "test='" + test + '\'' +
                '}';
    }
}
