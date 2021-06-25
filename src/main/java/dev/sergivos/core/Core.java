package dev.sergivos.core;

import dev.sergivos.core.listeners.PlayerListener;
import dev.sergivos.core.network.PacketListener;
import dev.sergivos.core.network.SimplePacket;
import dev.sergivos.messaging.MessagingService;
import dev.sergivos.messaging.packets.PacketManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.util.Arrays;
import java.util.Collections;

public final class Core extends JavaPlugin {
    private static final String detailedVersion = "{detailedVersion}";
    public static Core INSTANCE;
    private MessagingService messagingService;

    @Override
    public void onEnable() {
        INSTANCE = this;

        Collections.singletonList(
                new PlayerListener()
        ).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, this));

        PacketManager packetManager = new PacketManager();
        try {
            packetManager.register(SimplePacket.class, SimplePacket::new);

            messagingService = new MessagingService("TestPlugin", packetManager);
            messagingService.registerListener(new PacketListener());
        } catch(Exception e) {
            getSLF4JLogger().error("error creating MessagingService", e);
            Bukkit.shutdown();
            return;
        }

        Bukkit.getScheduler().runTaskLater(this, () -> messagingService.sendPacket(new SimplePacket("test", Component.text("testing"))), 5 * 20L);
    }

    @Override
    public void onDisable() {
        messagingService.close();
        System.out.println("Closed messaging service");

        INSTANCE = null;
    }

    public @MonotonicNonNull MessagingService messagingManager() {
        return this.messagingService;
    }
}
