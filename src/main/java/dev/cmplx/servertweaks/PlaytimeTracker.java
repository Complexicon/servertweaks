package dev.cmplx.servertweaks;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Team;

import dev.cmplx.servertweaks.commands.AfkCommand;

public class PlaytimeTracker implements Listener {

    private static Objective playtimeSec;
    private static Objective playtimeMin;
    private static Objective playtimeDisplay;
	private static Objective playtimeWeek;
	private static Objective afkTimer;
	private static Team afkTeam;

	private static void clearWeekPlaytime() {
		playtimeWeek.unregister();
		playtimeWeek = Util.getObjectiveSafe("playtimeWeek");
		Log.info("Reset all Weekly Playtime Limits.");
	}

	public static int getPlaytimeMinutes(Player p) {
		return playtimeMin.getScore(p.getName()).getScore();
	}

	public static void init() {
		if(Config.enableWeeklyLimit) {
			Cron.add(new Cron.Job(Config.playtimeResetCronjob, () -> clearWeekPlaytime()));
		}

		var globalScoreboard = Main.pluginRef.getServer().getScoreboardManager().getMainScoreboard();

		Cron.add(new Cron.Job("* * * * *", () -> {
			globalScoreboard
				.getEntries()
				.stream()
				.filter(v -> playtimeDisplay.getScore(v).isScoreSet())
				.forEach(v -> globalScoreboard.resetScores(v));

			var topFive = globalScoreboard
				.getEntries()
				.stream()
				.filter(v -> playtimeMin.getScore(v).isScoreSet())
				.map(v -> Map.entry(v, playtimeMin.getScore(v).getScore()))
				.sorted((v1, v2) -> v2.getValue() - v1.getValue())
				.limit(5)
				.toList();

			int spot = -1;
			for (var player : topFive) {
				var mins = player.getValue();
				var hours = mins / 60;
				mins = mins % 60;
				
				playtimeDisplay.getScore(Util.fixColor(String.format("%s &7(%dh%dm)", player.getKey(), hours, mins))).setScore(spot--);
			}

		}));

		playtimeDisplay = Util.getObjectiveSafe("playtimeDisplay", "§6Spielzeit - Top 5", Criteria.DUMMY, DisplaySlot.SIDEBAR);

		playtimeSec = Util.getObjectiveSafe("playtimeSec");
		playtimeMin = Util.getObjectiveSafe("playtimeMin");
		playtimeWeek = Util.getObjectiveSafe("playtimeWeek");
		afkTimer = Util.getObjectiveSafe("afkTimer");
		afkTeam = Util.getTeamSafe("afkPlayers");

		afkTeam.setSuffix(Util.fixColor("&c (AFK)"));
		afkTeam.setColor(ChatColor.GRAY);

		for(Player p : Bukkit.getOnlinePlayers()) {
			Util.setMetadata(p, "playtimeTask", Util.scheduler.runTaskTimer(Main.pluginRef, () -> handlePlaytime(p), 20, 20));
		}

	}

	@EventHandler
	public void onSpectate(PlayerTeleportEvent e) {
		if (e.getCause() != PlayerTeleportEvent.TeleportCause.SPECTATE) return;
		e.setCancelled(true);
		e.getPlayer().setSpectatorTarget(null);
	}

	public static void setAFK(Player p) {
		Bukkit.broadcastMessage(Util.fixColor(Config.afkMessage.replace("{player}", p.getName())));
		afkTeam.addEntry(p.getName());
		Util.setMetadata(p, "afkGamemode", p.getGameMode());
		p.setGameMode(GameMode.SPECTATOR);
	}

	public static boolean isAFK(Player p) {
		return afkTeam.hasEntry(p.getName());
	}

	public static void unAFK(Player p) {
		afkTeam.removeEntry(p.getName());

		var oldGamemode = Util.getMetadata(p, "afkGamemode", GameMode.class);
		p.setGameMode(oldGamemode != null ? oldGamemode : GameMode.SURVIVAL);

		Bukkit.broadcastMessage(Util.fixColor(Config.afkReturnMessage.replace("{player}", p.getName())));
		String party = Util.getPersistentString(p, new NamespacedKey(Main.pluginRef, "party"));
		if(party != null) {
			Util.getTeamSafe(party).addEntry(p.getName());
		}
	}

	private static void handlePlaytime(Player p) {
		
		int afkTime = afkTimer.getScore(p.getName()).getScore();

		if(afkTime == Config.afkTime - 1 && !Util.getPersistentBool(p, AfkCommand.stopAutoAFK)) {
			setAFK(p);
		}

		if(afkTime <= Config.afkTime) Util.modifyScore(afkTimer.getScore(p.getName()), old -> old + 1);
		else return;

		Util.modifyScore(playtimeSec.getScore(p.getName()), oldVal -> {
			if((oldVal + 1) % 60 == 0) {
				Util.modifyScore(playtimeMin.getScore(p.getName()), oldVal2 -> oldVal2 + 1);
				return 0;
			}

			return oldVal + 1;
		});

		Util.modifyScore(playtimeWeek.getScore(p.getName()), oldVal -> {
			if(Config.enableWeeklyLimit && oldVal + 1 >= Config.weeklyLimit)
				p.kickPlayer(Config.playtimeLimitReachedMessage);

			return oldVal + 1;
		});

	}

	public static int getWeekPlaytime(Player p) {
		return playtimeWeek.getScore(p.getName()).getScore();
	}

	@EventHandler
	public void onConnect(PlayerLoginEvent e) {
		if(Config.enableWeeklyLimit && getWeekPlaytime(e.getPlayer()) >= Config.weeklyLimit) {
			e.disallow(PlayerLoginEvent.Result.KICK_OTHER, Config.playtimeLimitReachedMessage);
		}
	}

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Util.setMetadata(p, "playtimeTask", Util.scheduler.runTaskTimer(Main.pluginRef, () -> handlePlaytime(p), 20, 20));
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if(e.getTo().distance(e.getFrom()) > 0) {
			String name = e.getPlayer().getName();
			Util.modifyScore(afkTimer.getScore(name), val -> 0);

			if(isAFK(e.getPlayer())) {
				unAFK(e.getPlayer());
			}

		}
	}

    @EventHandler
	public void onLeave(PlayerQuitEvent e) {
		BukkitTask t = Util.getMetadata(e.getPlayer(), "playtimeTask", BukkitTask.class);
		if(t != null) t.cancel();
	}

}
