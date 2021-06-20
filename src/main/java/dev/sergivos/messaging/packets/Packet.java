package dev.sergivos.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;


public interface Packet {

    /**
     * Deserializes the data from a {@link ByteBuf}
     *
     * @param buf The buffer to newInstance from
     */
    void read(final @NonNull ByteBuf buf);

    /**
     * Serializes the data into a {@link ByteBuf}
     *
     * @param buf The buffer to write data to
     */
    void write(final @NonNull ByteBuf buf);

}
