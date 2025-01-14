package dev.cmplx.servertweaks.commands;

import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.PlaytimeTracker;
import dev.cmplx.servertweaks.Util;

public class AfkCommand implements CommandExecutor {

	public static final NamespacedKey stopAutoAFK = new NamespacedKey(Main.pluginRef, "stopAutoAFK");

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player p)) {
			sender.sendMessage("Only players can run this command !");
			return false;
		}

		if (args.length == 1 && args[0].equals("toggle")) {
			
			if(Util.getPersistentBool(p, stopAutoAFK)) {
				Util.setPersistent(p, stopAutoAFK, false);
				p.sendMessage(Util.fixColor("&aAuto AFK Enabled"));
			} else {
				p.sendMessage(Util.fixColor("&cAuto AFK Disabled"));
				Util.setPersistent(p, stopAutoAFK, true);
			}

			return true;
		}

		if (PlaytimeTracker.isAFK(p)) {
			PlaytimeTracker.unAFK(p);
		} else {
			PlaytimeTracker.setAFK(p);
		}

		return true;

	}

}
