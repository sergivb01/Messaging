package dev.sergivos.core.messaging.packets;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * PacketManager provides a translation feature for custom packets. It allows to translate
 * a {@link Packet} into the appropiate {@link String}
 * <p>
 * The {@link String} type could change in a future for a more performant type
 */
public final class PacketManager {
    private final Map<Class<? extends Packet>, String> typeToId;
    private final Map<String, Constructor<? extends Packet>> idToType;

    /**
     * Creates an empty PacketManager
     */
    public PacketManager() {
        this.typeToId = Maps.newConcurrentMap();
        this.idToType = Maps.newConcurrentMap();
    }

    /**
     * Registers a new Packet into the manager
     *
     * @param clazz the {@link Packet} class to register
     * @throws IllegalArgumentException     if the {@link Packet} has already been registed in this {@link PacketManager}
     * @throws ReflectiveOperationException if a new {@link Packet} of the given type cannot be created
     */
    public void register(@NonNull Class<? extends Packet> clazz) throws IllegalArgumentException, ReflectiveOperationException {
        final String id = clazz.getSimpleName();
        if(typeToId.containsKey(clazz) || idToType.containsKey(id)) {
            throw new IllegalArgumentException("Packet " + clazz.getName() + " has already been registered.");
        }

        final Constructor<? extends Packet> constructor = clazz.getConstructor();
        // try and check if we'll be able to create new instances of this packet
        constructor.newInstance();

        typeToId.put(clazz, id);
        idToType.put(id, constructor);
    }

    /**
     * Get the id of a given {@link Packet}
     *
     * @param packet The packet to get the id from
     * @return returns {@code null} if the given packet is not registered or the appropriate ID.
     */
    public @Nullable String id(@NonNull Packet packet) {
        final Class<? extends @NonNull Packet> clazz = packet.getClass();
        final String id = clazz.getSimpleName();
        if(!typeToId.containsKey(clazz) || !idToType.containsKey(id)) {
            return null;
        }

        return id;
    }

    /**
     * Reads a {@link Packet} from a {@link ByteBuf}
     *
     * @param id  The id of the {@link Packet}
     * @param buf The buffer to read from
     * @return returns {@code null} if the {@code id} is not registered or the appropriate read {@link Packet}
     * @throws ReflectiveOperationException if the {@link Packet} of the {@code id} could not be created
     */
    public @Nullable Packet read(final @NonNull String id, final @NonNull ByteBuf buf) throws ReflectiveOperationException {
        final Constructor<? extends Packet> constructor = idToType.get(id);
        if(constructor == null) {
            return null;
        }

        // we could safely ignore these exceptions as we've verified that this packet
        // type works properly in the register method
        final Packet packet = constructor.newInstance();

        packet.read(buf);

        return packet;
    }

}
