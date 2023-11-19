package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SneakyMobs implements Listener {
	
	@EventHandler
	public void onSilenceMob(PlayerInteractAtEntityEvent e) {

		ItemStack interactItem = e.getPlayer().getInventory().getItem(e.getHand());
		ItemMeta itemMeta = interactItem.getItemMeta();
		if(e.getRightClicked() instanceof LivingEntity) {
			LivingEntity mob = (LivingEntity) e.getRightClicked();
			
			if(interactItem.getType() == Material.LEATHER_BOOTS && itemMeta.hasDisplayName() && itemMeta.getDisplayName().equals("sneaky boots")) {
				mob.setCustomName("sneaky " + mob.getName());
				mob.getEquipment().setBoots(interactItem);
				e.getRightClicked().setSilent(true);
				e.getPlayer().getInventory().setItem(e.getHand(), new ItemStack(Material.AIR));
				e.setCancelled(true);
			}
		}
	}

}
