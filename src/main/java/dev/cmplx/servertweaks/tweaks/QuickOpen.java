package dev.cmplx.servertweaks.tweaks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import dev.cmplx.servertweaks.Config;

public class QuickOpen implements Listener {
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR) return;
		if (!e.getPlayer().isSneaking()) return;

		switch (e.getPlayer().getInventory().getItemInMainHand().getType()) {
			case CRAFTING_TABLE:
				if(!Config.shiftOpenCraft) return;
				e.getPlayer().openWorkbench(null, true);
				break;
			case ENDER_CHEST:
				if(!Config.shiftOpenEnder) return;
				e.getPlayer().openInventory(e.getPlayer().getEnderChest());
				break;
			default:
				break;
		}

	}

}
