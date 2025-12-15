package dev.cmplx.servertweaks.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.InventoryGUI;
import dev.cmplx.servertweaks.Util;

public class TestCommand  implements CommandExecutor {

	class TestGUI extends InventoryGUI {
		public TestGUI() throws Exception {
			super("Test GUI");

			addItem(new ItemStack(Material.ECHO_SHARD), 1, 34, e->{});
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!sender.hasPermission("servertweaks.debug")) {
			sender.sendMessage(Util.fixColor("&4No Permission to use this Command!"));
			return false;
		}

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this command !");
			return false;
		}
		Player p = (Player)sender;

		var r = new ItemStack(Material.FIREWORK_ROCKET);

		p.getInventory().addItem(r);

		return true;

	}

}
