package dev.cmplx.servertweaks.commands;

import java.net.URL;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.tweaks.items.PictureMap;

public class PictureMapCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(Util.fixColor("&cMust be Player!"));
			return true;
		}

		Player p = (Player) sender;

		var item = p.getInventory().getItemInMainHand();

		if (item.getType() != Material.MAP) {
			sender.sendMessage(Util.fixColor("&cMust hold Empty Map!"));
			return true;
		}

		if (args.length != 1) {
			sender.sendMessage("Usage: /picturemap https://i.postimg.cc/<...>");
			return true;
		}

		try {

			var url = new URL(args[0]);

			if (!url.getHost().equals("i.postimg.cc")) {
				sender.sendMessage(Util.fixColor("&cPicture must be from https://i.postimg.cc !"));
				return true;
			}

			if (!url.getPath().endsWith(".png")) {
				sender.sendMessage(Util.fixColor("&cPicture must be a .png file!"));
				return true;
			}

			Bukkit.getScheduler().runTaskAsynchronously(Main.pluginRef, () -> {
				try {
					var image = ImageIO.read(url);

					Bukkit.getScheduler().runTask(Main.pluginRef, () -> {
						var newMap = PictureMap.createNewMap(p.getWorld(), image);
						
						if (newMap == null) {
							sender.sendMessage(Util.fixColor("&cSomething went wrong!"));
							return;
						}

						item.setAmount(item.getAmount() - 1);
						p.getInventory().addItem(newMap);

					});

				} catch (Exception e) {
					sender.sendMessage(Util.fixColor("&cSomething went wrong!"));
					e.printStackTrace();
				}
			});

		} catch (Exception e) {
			sender.sendMessage(Util.fixColor("&cMalformed URL!"));
		}

		return true;
	}

}
