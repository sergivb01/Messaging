package dev.sergivos.core.listeners;

import dev.sergivos.core.Core;
import dev.sergivos.core.messaging.packets.Packet;
import dev.sergivos.core.messaging.packets.types.SimplePacket;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws Exception {
        final Player player = event.getPlayer();

        final Packet packet = new SimplePacket(player.getName() + " ha entrado");

        Core.INSTANCE.messagingManager().sendPacket(packet, true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        final Packet packet = new SimplePacket(player.getName() + " ha salido");

        try {
            Core.INSTANCE.messagingManager().sendPacket(packet, true);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
