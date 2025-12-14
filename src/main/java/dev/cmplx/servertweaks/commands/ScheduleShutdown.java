package dev.cmplx.servertweaks.commands;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitTask;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.i18n;

public class ScheduleShutdown implements CommandExecutor, TabCompleter {

	List<String> subcommands = Arrays.asList("time", "countdown", "cancel");

	List<BukkitTask> pendingTasks = new ArrayList<>();

	void addShutdownWarningTask(long time, long countdown, String reason) {
		if (countdown < time) return;

		pendingTasks.add(Bukkit.getScheduler().runTaskLater(Main.pluginRef, () -> {
			var until = Duration.ofSeconds(time).toString().substring(2).toLowerCase();
			var warning = i18n.SERVER_SHUTDOWN_COUNTDOWN.fmt(i18n.param("countdown", until));
			Bukkit.broadcastMessage(warning + "§7>> " + reason);
			for (var p : Bukkit.getOnlinePlayers()) {
				p.sendTitle(warning, reason, 5, 60, 5);
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
			}
		}, (countdown - time) * 20));

	}

	void scheduleShutdown(long countdown, String reason) {
		addShutdownWarningTask(15 * 60, countdown, reason);
		addShutdownWarningTask(10 * 60, countdown, reason);
		addShutdownWarningTask(5 * 60, countdown, reason);
		addShutdownWarningTask(2 * 60, countdown, reason);
		addShutdownWarningTask(60, countdown, reason);
		addShutdownWarningTask(30, countdown, reason);
		addShutdownWarningTask(10, countdown, reason);
		addShutdownWarningTask(5, countdown, reason);
		addShutdownWarningTask(4, countdown, reason);
		addShutdownWarningTask(3, countdown, reason);
		addShutdownWarningTask(2, countdown, reason);
		addShutdownWarningTask(1, countdown, reason);

		pendingTasks.add(Bukkit.getScheduler().runTaskLater(Main.pluginRef, () -> Bukkit.getServer().shutdown(), countdown * 20));
		// pendingTasks.add(Bukkit.getScheduler().runTaskLater(Main.pluginRef, () -> Log.debug("mock shutdown"), countdown * 20));

	}

	DateTimeFormatter timestampParser = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("servertweaks.scheduleshutdown")) {
			sender.sendMessage(Util.fixColor("&4No Permission to use this Command!"));
			return false;
		}

		args = Util.parseCommandArgs(args);

		if (args.length == 1 && args[0].equals("cancel")) {
			for (var task : pendingTasks) {
				task.cancel();
			}
			pendingTasks.clear();
			sender.sendMessage(Util.fixColor("&aPending Shutdown Canceled"));
			return true;
		}

		if (args.length < 3 || !subcommands.contains(args[0])) {
			sender.sendMessage(Util.fixColor("&4Usage: /scheduleshutdown <time|countdown> <19:30 | 60s> <reasoning>"));
			return true;
		}

		long countdown = 0;

		if (args[0].equals("time")) {
			try {
				var targetTime = LocalDateTime.of(LocalDate.now(), LocalTime.parse(args[1], timestampParser));

				if (Duration.between(LocalDateTime.now(), targetTime).isNegative()) {
					targetTime = targetTime.plusDays(1);
				}

				countdown = Duration.between(LocalDateTime.now(), targetTime).toSeconds();

			} catch (Exception e) {
				sender.sendMessage(Util.fixColor("&4Couldn't parse timestamp '" + args[1] + "'"));
			}
		} else if (args[0].equals("countdown")) {
			try {
				var duration = Duration.parse("PT" + args[1].toUpperCase());
				countdown = duration.toSeconds();
			} catch (Exception e) {
				sender.sendMessage(Util.fixColor("&4Couldn't parse duration '" + args[1] + "'"));
			}
		}

		scheduleShutdown(countdown, Util.fixColor(args[2]));
		sender.sendMessage(Util.fixColor("&aScheduled Shutdown in " + Duration.ofSeconds(countdown).toString().substring(2).toLowerCase() + " seconds"));

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		return args.length == 1 ? subcommands.stream().filter(v -> v.startsWith(args[0])).toList() : null;
	}

}
