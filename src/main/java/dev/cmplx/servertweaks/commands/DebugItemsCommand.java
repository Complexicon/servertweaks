package dev.cmplx.servertweaks.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.Util;

public class DebugItemsCommand implements CommandExecutor {

	public static List<ItemStack> DebugItems = new ArrayList<>();

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("servertweaks.debug")) {
			sender.sendMessage(Util.fixColor("&4No Permission to use this Command!"));
			return false;
		}
		
		if (!(sender instanceof Player p)) {
			sender.sendMessage("Only players can run this command !");
			return false;
		}

		var inv = Bukkit.createInventory(null, 9 * ((DebugItems.size() / 9) + 1));

		inv.addItem(DebugItems.toArray(new ItemStack[0]));

		p.openInventory(inv);

		return true;
	}

}
