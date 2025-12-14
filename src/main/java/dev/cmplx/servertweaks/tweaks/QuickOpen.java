package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.MenuType;

import dev.cmplx.servertweaks.Config;

public class QuickOpen implements Listener {
	
	@EventHandler
	public void onRightClick(PlayerInteractEvent e) {
		if (e.getAction() != Action.RIGHT_CLICK_AIR) return;
		if (!e.getPlayer().isSneaking()) return;

		var p = e.getPlayer();

		switch (p.getInventory().getItemInMainHand().getType()) {
			case CRAFTING_TABLE:
				if(!Config.shiftOpenCraft) return;
				p.playSound(p.getLocation(), Sound.ENTITY_HORSE_ARMOR, 1, 1);
				p.openInventory(MenuType.CRAFTING.builder().build(p));
				break;
			case ENDER_CHEST:
				if(!Config.shiftOpenEnder) return;
				p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_TELEPORT, 1, 2);
				p.openInventory(p.getEnderChest());
				break;
			default:
				break;
		}

	}

}
