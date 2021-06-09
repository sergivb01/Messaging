package dev.sergivos.core.messaging.packets;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * PacketManager provides a translation feature for custom packets. It allows to translate
 * a {@link Packet} into the appropiate {@link String}
 * <p>
 * The {@link String} type could change in a future for a more performant type
 */
public final class PacketManager {
    private final Set<Class<? extends Packet>> registeredClasses;
    private final Map<String, PacketSupplier<? extends Packet>> idToType;

    /**
     * Creates an empty PacketManager
     */
    public PacketManager() {
        this.registeredClasses = ConcurrentHashMap.newKeySet();
        this.idToType = Maps.newConcurrentMap();
    }

    /**
     * Registers a new Packet into the manager
     *
     * @param clazz the {@link Packet} class to register
     * @throws IllegalArgumentException if the {@link Packet} or {@link Class} has already been registered in this {@link PacketManager}
     */
    public <T extends Packet> void register(final @NonNull Class<T> clazz, final @NonNull PacketSupplier<T> supplier) throws IllegalArgumentException {
        if(registeredClasses.contains(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has already been registered.");
        }

        final String id = clazz.getSimpleName();
        if(idToType.containsKey(id)) {
            throw new IllegalArgumentException("A class with ID " + clazz.getSimpleName() + " has already been registered. (" + idToType.get(id).getClass().getName() + ")");
        }

        registeredClasses.add(clazz);
        idToType.put(id, supplier);
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
        if(!registeredClasses.contains(clazz) || !idToType.containsKey(id)) {
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
     */
    public @Nullable Packet read(final @NonNull String id, final @NonNull ByteBuf buf) {
        final PacketSupplier<? extends Packet> supplier = idToType.get(id);
        if(supplier == null) {
            return null;
        }

        return supplier.create(buf);
    }

}
