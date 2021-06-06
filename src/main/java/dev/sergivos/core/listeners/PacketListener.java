package dev.sergivos.core.listeners;

import dev.sergivos.core.messaging.packets.types.SimplePacket;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PacketListener implements Listener {

    @EventHandler
    public void onPacket(SimplePacket packet) {
        Bukkit.broadcastMessage("[SimplePacket] " + packet);
    }

}
