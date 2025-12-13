package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class LeashableVillagers implements Listener {

	@EventHandler
	void onVillagerLeash(PlayerInteractEntityEvent e) {
		if (!e.getPlayer().isSneaking()) return;
		if (e.getPlayer().getInventory().getItemInMainHand().getType() != Material.LEAD) return;
		if ((e.getRightClicked() instanceof Villager v)) {
			var item = e.getPlayer().getInventory().getItemInMainHand();
			item.setAmount(item.getAmount() - 1);
			v.setLeashHolder(e.getPlayer());
			e.setCancelled(true);
		}
	}

}
