package dev.sergivos.messaging.packets;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * PacketManager provides a translation feature for custom packets. It allows to translate a {@link
 * Packet} into the appropriate {@link String}
 * <p>
 * The {@link String} type could change in a future for a more performant type
 */
public final class PacketManager {

  private final @NonNull Function<Class<? extends Packet>, String> mapperFunc;
  private final @NonNull Set<Class<? extends Packet>> registeredClasses;
  private final @NonNull Map<@NonNull String, @NonNull Supplier<? extends Packet>> idToSupplier;

  /**
   * Creates an empty PacketManager
   */
  public PacketManager() {
    this(Class::getSimpleName);
  }

  /**
   * Creates an empty PacketManager
   *
   * @param mapperFunc A function that will map a Packet to the ID
   */
  public PacketManager(final @NonNull Function<Class<? extends Packet>, String> mapperFunc) {
    this.registeredClasses = ConcurrentHashMap.newKeySet();
    this.idToSupplier = Maps.newConcurrentMap();
    this.mapperFunc = mapperFunc;
  }

  /**
   * Registers a new Packet into the manager
   *
   * @param <T>      class that extends {@link Packet}
   * @param clazz    the {@link Packet} class to register
   * @param supplier the {@link Supplier} that provides a new instance of {@code clazz}.
   * @throws IllegalArgumentException if the {@link Packet} or {@link Class} has already been
   *                                  registered in this {@link PacketManager}
   */
  public <T extends Packet> void register(final @NonNull Class<T> clazz,
      final @NonNull Supplier<T> supplier) throws IllegalArgumentException {
    if (registeredClasses.contains(clazz)) {
      throw new IllegalArgumentException(
          "Class " + clazz.getName() + " has already been registered.");
    }

    final String id = mapperFunc.apply(clazz);
    if (idToSupplier.containsKey(id)) {
      throw new IllegalArgumentException(
          "A class with ID " + id + " has already been registered. (" + idToSupplier.get(id)
              .getClass().getName() + ")");
    }

    registeredClasses.add(clazz);
    idToSupplier.put(id, supplier);
  }

  /**
   * Unregisters a new Packet into the manager
   *
   * @param <T>   class that extends {@link Packet}
   * @param clazz the {@link Packet} class to register
   * @throws IllegalArgumentException if the {@link Packet} or {@link Class} is not registered in
   *                                  this {@link PacketManager}
   */
  public <T extends Packet> void unregister(final @NonNull Class<T> clazz)
      throws IllegalArgumentException {
    if (!registeredClasses.contains(clazz)) {
      throw new IllegalArgumentException("Class " + clazz.getName() + " is not registered.");
    }

    final String id = mapperFunc.apply(clazz);
    registeredClasses.remove(clazz);
    idToSupplier.remove(id);
  }

  /**
   * Get the id of a given {@link Packet}
   *
   * @param packet The packet to get the id from
   * @return the appropriate ID of the {@code packet} or {@code null} if the given packet is not
   * registered.
   */
  public @Nullable String id(final @NonNull Packet packet) {
    final Class<? extends @NonNull Packet> clazz = packet.getClass();
    final String id = mapperFunc.apply(clazz);
    if (!registeredClasses.contains(clazz) || !idToSupplier.containsKey(id)) {
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
    final Supplier<? extends Packet> supplier = idToSupplier.get(id);
    if (supplier == null) {
      return null;
    }

    return supplier.get();
  }

}
