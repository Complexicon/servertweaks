package dev.cmplx.servertweaks.commands;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.PlaytimeTracker;
import dev.cmplx.servertweaks.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;

public class PlaytimeCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		var globalScoreboard = Main.pluginRef.getServer().getScoreboardManager().getMainScoreboard();

		var playtimes = globalScoreboard
				.getEntries()
				.stream()
				.map(v -> Map.entry(v, PlaytimeTracker.getPlaytimeMinutes(v)))
				.filter(entry -> entry.getValue() > 0)
				.sorted((v1, v2) -> v2.getValue() - v1.getValue())
				.map((entry) -> {
					var player = entry.getKey();
					var mins = entry.getValue();

					var hours = mins / 60;
					mins = mins % 60;
					return Map.entry(player, String.format("%dh%dm", hours, mins));
				}).toList();

		var msgsToSend = playtimes.stream().map(entry -> Util.fixColor(String.format("- %s &7(%s)", entry.getKey(), entry.getValue())));
		
		sender.sendMessage(msgsToSend.toArray(String[]::new));

		var copy = new TextComponent("[Hier Klicken]");
		copy.setColor(ChatColor.GREEN);
		copy.setClickEvent(new ClickEvent(Action.COPY_TO_CLIPBOARD, String.join("\n", playtimes.stream().map(entry -> entry.getKey() + " " + entry.getValue()).toList())));
		sender.spigot().sendMessage(
			new TextComponent(Util.fixColor("Um die Liste zu Kopieren ")),
			copy
		);

		return true;
	}

}
