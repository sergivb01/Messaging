package dev.sergivos.core.messaging.compression;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NoCompression implements Compression {
    private static final ByteBufAllocator bufferPool = PooledByteBufAllocator.DEFAULT;

    @Override
    public byte[] compress(@NonNull ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), data);
        return data;
    }

    @Override
    public @NonNull ByteBuf decompress(byte[] data) {
        final ByteBuf buf = bufferPool.buffer(data.length, data.length);
        buf.writeBytes(data);
        return buf;
    }

}
