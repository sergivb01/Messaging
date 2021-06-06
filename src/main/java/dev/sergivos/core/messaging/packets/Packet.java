package dev.sergivos.core.messaging.packets;

import io.netty.buffer.ByteBuf;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

// TODO: replace Bukkit events for platform-independent system
public abstract class Packet extends Event {
    private static final HandlerList handlers = new HandlerList();
    private @MonotonicNonNull final String sender;

    public Packet() {
        this.sender = "Unknown";
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    public @NonNull String sender() {
        return this.sender;
    }

    public abstract void read(@NonNull final ByteBuf buf);

    public abstract void write(@NonNull final ByteBuf buf);

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
