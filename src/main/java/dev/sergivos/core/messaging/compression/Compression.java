package dev.sergivos.core.messaging.compression;

import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface Compression {

    byte[] compress(@NonNull final ByteBuf buf);

    @NonNull ByteBuf decompress(byte[] data);

}
