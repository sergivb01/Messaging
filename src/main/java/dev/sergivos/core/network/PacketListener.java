package dev.sergivos.core.network;

import com.google.common.eventbus.Subscribe;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;

public class PacketListener {

    @Subscribe
    public void onPacket(SimplePacket packet) {
        Bukkit.broadcast(Component.text("[SimplePacket] ", NamedTextColor.AQUA)
                .append(Component.text(packet.message(), NamedTextColor.WHITE)));
    }

}
