package dev.sergivos.core.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.jetbrains.annotations.NotNull;

/**
 * PacketSupplier is a way to easly create a new instance of a {@link Packet}
 * without the use of {@code Reflection}. It is used to register a {@link Packet}
 * to a {@link PacketManager}.
 *
 * <h3>Can be used as follows</h3>
 * <pre>
 *     packetManager.register(SimplePacket.class, new PacketSupplier<SimplePacket>() {
 *         \@Override
 *         public @NotNull SimplePacket create(@NotNull ByteBuf buffer) {
 *             return new SimplePacket(buffer);
 *         }
 *     });
 * </pre>
 * <p>
 * Which simplified will result in
 * <pre>
 *     packetManager.register(SimplePacket.class, SimplePacket::new);
 * </pre>
 */
public interface PacketSupplier<T extends Packet> {

    /**
     * Creates a new instance of a {@link Packet} of {@code T}
     *
     * @param buffer The buffer to create the instance from
     * @return returns a new {@link Packet} {@code T}
     */
    @NotNull T create(final @NotNull ByteBuf buffer);
}
