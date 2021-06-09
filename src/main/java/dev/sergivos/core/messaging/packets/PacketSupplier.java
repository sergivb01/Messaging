package dev.sergivos.core.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

public interface PacketSupplier<T extends Packet> {
    @NotNull T create(final @NotNull ByteBuf buffer);
}
