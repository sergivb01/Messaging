package dev.sergivos.messaging.compression;

import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface MessagingCompression {

    /**
     * Compresses a {@link ByteBuf} into a byte array
     *
     * @param buffer The buffer containing the data to compress
     * @return returns a byte array containing the compressed {@code buffer}
     */
    byte[] compress(final @NonNull ByteBuf buffer);

    /**
     * Decompresses the given data into a {@link ByteBuf}
     *
     * @param data The byte array containing the data to decompress
     * @return returns a {@link ByteBuf} containing the decompressed data
     */
    @NonNull ByteBuf decompress(byte[] data);

}
