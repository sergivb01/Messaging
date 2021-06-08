package dev.sergivos.core;

import dev.sergivos.core.listeners.PacketListener;
import dev.sergivos.core.listeners.PlayerListener;
import dev.sergivos.core.messaging.MessagingService;
import dev.sergivos.core.messaging.packets.PacketManager;
import dev.sergivos.core.messaging.packets.types.SimplePacket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.slf4j.Logger;

import java.util.Arrays;

public final class Core extends JavaPlugin {
    private static final String version = "{version}";
    public static Core INSTANCE;
    private MessagingService messagingService;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Arrays.asList(
                new PlayerListener(),
                new PacketListener()
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

        PacketManager packetManager = new PacketManager();
        try {
            packetManager.register(SimplePacket.class);

            messagingService = new MessagingService("TestPlugin", packetManager, getSLF4JLogger());
        } catch(Exception e) {
            getSLF4JLogger().error("error creating MessagingService", e);
            Bukkit.shutdown();
            return;
        }

        getSLF4JLogger().info("MessagingService created!");

        Bukkit.getScheduler().runTaskLater(this, () -> {
            messagingService.sendPacket(new SimplePacket("test", "hola"));
        }, 5 * 20L);
    }

    @Override
    public void onDisable() {
        messagingService.close();
        System.out.println("Closed messaging service");

        INSTANCE = null;
    }

    public @MonotonicNonNull Logger logger() {
        return this.getSLF4JLogger();
    }

    public @MonotonicNonNull MessagingService messagingManager() {
        return this.messagingService;
    }
}
