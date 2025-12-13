package dev.cmplx.servertweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ChatEvents implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.setJoinMessage(i18n.JOIN_MESSAGE.fmt(i18n.param("player", e.getPlayer().getName())));
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		e.setQuitMessage(i18n.LEAVE_MESSAGE.fmt(i18n.param("player", e.getPlayer().getName())));
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {
		e.setDeathMessage(Util.fixColor(Config.deathMessageFormat.replace("{deathMessage}", e.getDeathMessage())));
	}

    @EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		e.setFormat(Util.fixColor(Config.chatMessageFormat.replace("{player}", "%1$s").replace("{message}", "%2$s")));
		e.setMessage(Util.fixColor(e.getMessage()));
	}

}
