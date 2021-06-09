package dev.sergivos.core.listeners;

import com.google.common.eventbus.Subscribe;
import dev.sergivos.core.messaging.packets.types.SimplePacket;
import org.bukkit.Bukkit;

public class PacketListener {

    @Subscribe
    public void onPacket(SimplePacket packet) {
        Bukkit.broadcastMessage("[SimplePacket] " + packet);
    }

}
