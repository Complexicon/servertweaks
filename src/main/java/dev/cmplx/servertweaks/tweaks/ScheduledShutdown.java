package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Bukkit;

import dev.cmplx.servertweaks.Config;
import dev.cmplx.servertweaks.Cron;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;

public class ScheduledShutdown {

	private ScheduledShutdown() {}

	public static void init() {
		if (!Config.scheduledServerShutdown) return;


		Cron.add(new Cron.Job(Config.scheduledServerShutdownCronjob, ScheduledShutdown::initShutdownSequence));
	}

	static void initShutdownSequence() {

		Bukkit.broadcastMessage(Util.fixColor(""));

		Main.pluginRef.getServer().shutdown();
	}

}
