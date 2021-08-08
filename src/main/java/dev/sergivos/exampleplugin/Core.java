package dev.sergivos.exampleplugin;

import dev.sergivos.exampleplugin.listeners.PlayerListener;
import dev.sergivos.exampleplugin.network.PacketListener;
import dev.sergivos.exampleplugin.network.SimplePacket;
import dev.sergivos.messaging.MessagingService;
import dev.sergivos.messaging.brokers.NatsBroker;
import dev.sergivos.messaging.packets.PacketManager;
import java.util.Collections;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

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

      final NatsBroker broker = new NatsBroker(
          "nats://127.0.0.1:4222,nats://127.0.0.1:5222,nats://127.0.0.1:6222");
      messagingService = new MessagingService("TestPlugin", packetManager, broker);
      messagingService.registerListener(new PacketListener());
    } catch (Exception e) {
      getSLF4JLogger().error("error creating MessagingService", e);
      Bukkit.shutdown();
      return;
    }

    Bukkit.getScheduler().runTaskLater(this,
        () -> messagingService.sendPacket(new SimplePacket("test", Component.text("testing"))),
        5 * 20L);
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
