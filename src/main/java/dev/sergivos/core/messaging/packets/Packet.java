package dev.sergivos.core.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

// TODO: replace Bukkit events for platform-independent system
public abstract class Packet extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public abstract void read(final @NonNull ByteBuf buf);

    public abstract void write(final @NonNull ByteBuf buf);

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
