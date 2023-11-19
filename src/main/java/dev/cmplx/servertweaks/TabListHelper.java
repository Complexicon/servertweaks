package dev.cmplx.servertweaks;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TabListHelper implements Runnable, Listener {
	
	double curTPS;

	TabListHelper() {
		Bukkit.getScheduler().runTaskTimerAsynchronously(Main.pluginRef, this, 0, 20);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().setPlayerListHeader(Util.fixColor(Config.motdHeader) + "\n");
	}

	void setTabListFooter(Player p) {

		List<String> footer = new ArrayList<>();
		footer.add("");

		String topStats = "";
		topStats += "&7TPS: &6" + String.format("%.2f", curTPS);
		topStats += " &7Ping: &6" + p.getPing() + "ms";
		topStats += " &7Entities: &6" + p.getWorld().getEntities().size();
		
		footer.add(topStats);

		if(Config.enableWeeklyLimit) {

			int alreadyPlayed = PlaytimeTracker.getWeekPlaytime(p);

			ChatColor color;
	
			float percentagePlayed = (float)alreadyPlayed / Config.weeklyLimit;
	
			if(percentagePlayed > 0.8) color = ChatColor.RED;
			else if(percentagePlayed > 0.5) color = ChatColor.GOLD;
			else color = ChatColor.GREEN;
	
			int remainingTime = Config.weeklyLimit - alreadyPlayed;
	
			int hours = remainingTime / 3600;
			int minutes = (remainingTime % 3600) / 60;
			int seconds = remainingTime % 60;

			footer.add("&7Deine übrige Spielzeit: " + color + String.format("%02d:%02d:%02d", hours, minutes, seconds));
		}

		footer.add("");
		p.setPlayerListFooter(Util.fixColor(String.join("\n", footer)));

	}

	@Override
	public void run() {
		curTPS = Util.getTPSLastMin();
		for(Player player : Bukkit.getOnlinePlayers()) {
			Bukkit.getScheduler().runTask(Main.pluginRef, () -> setTabListFooter(player));
		}
	}

}
