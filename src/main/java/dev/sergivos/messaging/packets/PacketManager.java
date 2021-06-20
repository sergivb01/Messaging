package dev.sergivos.messaging.packets;

import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * PacketManager provides a translation feature for custom packets. It allows to translate
 * a {@link Packet} into the appropiate {@link String}
 * <p>
 * The {@link String} type could change in a future for a more performant type
 */
public final class PacketManager {
    private final Set<Class<? extends Packet>> registeredClasses;
    private final Map<String, Supplier<Packet>> idToSupplier;

    /**
     * Creates an empty PacketManager
     */
    public PacketManager() {
        this.registeredClasses = ConcurrentHashMap.newKeySet();
        this.idToSupplier = Maps.newConcurrentMap();
    }

    /**
     * Registers a new Packet into the manager
     *
     * @param clazz the {@link Packet} class to register
     * @throws IllegalArgumentException if the {@link Packet} or {@link Class} has already been registered in this {@link PacketManager}
     */
    public <T extends Packet> void register(final @NonNull Class<T> clazz, final @NonNull Supplier<Packet> supplier) throws IllegalArgumentException {
        if(registeredClasses.contains(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " has already been registered.");
        }

        final String id = clazz.getSimpleName();
        if(idToSupplier.containsKey(id)) {
            throw new IllegalArgumentException("A class with ID " + clazz.getSimpleName() + " has already been registered. (" + idToSupplier.get(id).getClass().getName() + ")");
        }

        registeredClasses.add(clazz);
        idToSupplier.put(id, supplier);
    }

    /**
     * Get the id of a given {@link Packet}
     *
     * @param packet The packet to get the id from
     * @return the appropriate ID of the {@code packet} or {@code null} if the given packet is not registered.
     */
    public @Nullable String id(@NonNull Packet packet) {
        final Class<? extends @NonNull Packet> clazz = packet.getClass();
        final String id = clazz.getSimpleName();
        if(!registeredClasses.contains(clazz) || !idToSupplier.containsKey(id)) {
            return null;
        }

        return id;
    }

    /**
     * Creates a new empty instance of a {@link Packet}.
     *
     * @param id The id of the {@link Packet}
     * @return a new instance of {@link Packet} or {@code null} if the {@code id} is not registered
     */
    public @Nullable Packet newInstance(final @NonNull String id) {
        final Supplier<Packet> supplier = idToSupplier.get(id);
        if(supplier == null) {
            return null;
        }

        return supplier.get();
    }

}
