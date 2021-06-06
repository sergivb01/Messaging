package dev.sergivos.core;

import dev.sergivos.core.messaging.MessagingService;
import dev.sergivos.core.listeners.PacketListener;
import dev.sergivos.core.listeners.PlayerListener;
import dev.sergivos.core.messaging.packets.PacketManager;
import dev.sergivos.core.messaging.packets.types.SimplePacket;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Arrays;

public final class Core extends JavaPlugin {
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
        } catch(Exception exception) {
            exception.printStackTrace();
        }

        try {
            messagingService = new MessagingService(packetManager, getSLF4JLogger());
        } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        messagingService.close();
        System.out.println("Closed messaging service");

        INSTANCE = null;
    }

    public MessagingService messagingManager() {
        return this.messagingService;
    }
}
