package dev.sergivos.messaging.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NoCompression implements MessagingCompression {
    private static final ByteBufAllocator ALLOCATOR = PooledByteBufAllocator.DEFAULT;

    @Override
    public byte[] compress(final @NonNull ByteBuf buf) {
        int uncompressedBytes = buf.writerIndex();
        byte[] data = new byte[uncompressedBytes];
        buf.readBytes(data);
        return data;
    }

    @Override
    public @NonNull ByteBuf decompress(byte[] data) {
        final ByteBuf buf = ALLOCATOR.buffer(data.length, data.length);
        buf.writeBytes(data);
        return buf;
    }

}
