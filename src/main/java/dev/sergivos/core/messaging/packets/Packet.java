package dev.sergivos.core.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Packet {
    void read(final @NonNull ByteBuf buf);

    void write(final @NonNull ByteBuf buf);

}
