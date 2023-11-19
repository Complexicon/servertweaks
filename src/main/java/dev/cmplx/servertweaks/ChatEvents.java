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
		e.setJoinMessage(Util.fixColor(Config.joinMessage.replace("{player}", e.getPlayer().getName())));
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		e.setQuitMessage(Util.fixColor(Config.leaveMessage.replace("{player}", e.getPlayer().getName())));
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {
		e.setDeathMessage(Util.fixColor(Config.deathMessage.replace("{deathMessage}", e.getDeathMessage())));
	}

    @EventHandler
	public void onChat(AsyncPlayerChatEvent e) {
		e.setFormat(Util.fixColor(Config.chatFormat.replace("{player}", "%1$s").replace("{message}", "%2$s")));
		e.setMessage(Util.fixColor(e.getMessage()));
	}

}
