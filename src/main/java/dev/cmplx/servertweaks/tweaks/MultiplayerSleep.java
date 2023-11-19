package dev.cmplx.servertweaks.tweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class MultiplayerSleep implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		e.getPlayer().setSleepingIgnored(true);
	}

}
