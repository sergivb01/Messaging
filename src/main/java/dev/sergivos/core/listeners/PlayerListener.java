package dev.sergivos.core.listeners;

import dev.sergivos.core.Core;
import dev.sergivos.core.network.SimplePacket;
import dev.sergivos.messaging.packets.Packet;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    final Packet packet = new SimplePacket("test", Objects.requireNonNull(event.joinMessage()));

    Core.INSTANCE.messagingManager().sendPacket(packet);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    final Packet packet = new SimplePacket("test", Objects.requireNonNull(event.quitMessage()));

    Core.INSTANCE.messagingManager().sendPacket(packet);
  }

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    final Packet packet = new SimplePacket("test", Objects.requireNonNull(event.message()));

    Core.INSTANCE.messagingManager().sendPacket(packet);
  }

}
