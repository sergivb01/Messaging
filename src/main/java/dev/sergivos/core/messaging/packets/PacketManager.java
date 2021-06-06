package dev.sergivos.core.messaging.packets;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class PacketManager {
    private final Map<Class<? extends Packet>, String> typeToId;
    private final Map<String, Constructor<? extends Packet>> idToType;

    public PacketManager() {
        this.typeToId = Maps.newConcurrentMap();
        this.idToType = Maps.newConcurrentMap();
    }

    public int numerRegisteredPackets() {
        return idToType.size();
    }

    public void register(@NonNull Class<? extends Packet> clazz) throws Exception {
        final String id = clazz.getSimpleName();
        if(typeToId.containsKey(clazz) || idToType.containsKey(id)) {
            throw new Exception("Packet " + clazz.getName() + " has already been registered.");
        }

        final Constructor<? extends Packet> constructor = clazz.getConstructor();
        // try and check if we'll be able to create new instances of this packet
        constructor.newInstance();

        typeToId.put(clazz, id);
        idToType.put(id, constructor);
    }

    public @Nullable String id(@NonNull Packet packet) {
        final Class<? extends @NonNull Packet> clazz = packet.getClass();
        final String id = clazz.getSimpleName();
        if(!typeToId.containsKey(clazz) || !idToType.containsKey(id)) {
            return null;
        }

        return id;
    }

    public @Nullable Packet read(@NonNull final String id, @NonNull final ByteBuf buf) throws InvocationTargetException, InstantiationException, IllegalAccessException {
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
