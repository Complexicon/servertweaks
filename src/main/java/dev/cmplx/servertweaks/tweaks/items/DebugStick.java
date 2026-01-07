package dev.cmplx.servertweaks.tweaks.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.cmplx.servertweaks.ItemStackBuilder;
import dev.cmplx.servertweaks.Log;
import dev.cmplx.servertweaks.Main;
import dev.cmplx.servertweaks.Util;
import dev.cmplx.servertweaks.commands.DebugItemsCommand;

public class DebugStick implements Listener {
	public static NamespacedKey debug_stick = new NamespacedKey(Main.pluginRef, "debug_stick");
	public static ItemStack debugStick = new ItemStackBuilder(Material.STICK).setName("&aDebugStick").setPersistent(debug_stick, true).build();

	static { DebugItemsCommand.DebugItems.add(debugStick); }

	boolean isDebugStick(ItemStack i) {
		if (i == null) return false;
		return Util.getPersistentBool(i.getItemMeta(), debug_stick);
	}

	@EventHandler
	void debugBlock(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!isDebugStick(e.getItem())) return;

		var block = e.getClickedBlock();

		Log.debug(block);

		e.setCancelled(true);
	}

	@EventHandler
	void debugEntity(PlayerInteractAtEntityEvent e) {
		if (!isDebugStick(e.getPlayer().getInventory().getItemInMainHand())) return;

		Log.debug(e.getRightClicked());

		e.setCancelled(true);
	}

}
