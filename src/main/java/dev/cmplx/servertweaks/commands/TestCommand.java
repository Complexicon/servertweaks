package dev.cmplx.servertweaks.commands;

import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.InventoryGUI;

public class TestCommand  implements CommandExecutor {

	class TestGUI extends InventoryGUI {
		public TestGUI() throws Exception {
			super("Test GUI");

			addItem(new ItemStack(Material.ECHO_SHARD), 1, 34, e->{});
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("Only players can run this command !");
			return false;
		}
		Player p = (Player)sender;

		var b = p.getWorld().getBlockAt(0, 255, 0);
		b.setType(Material.OAK_SIGN);
		p.openSign((Sign)b.getState());
		b.setType(Material.AIR);

		// try {
		// 	p.openInventory(new TestGUI().getInventory());
		// } catch (Exception e) {
		// 	e.printStackTrace();
		// }

		// p.openInventory(new TextInput("test input", true, data -> {
		// 	Log.debug(data.getKey() + " aux item: " + data.getValue());
		// }).getInventory());

		return true;

	}

}
