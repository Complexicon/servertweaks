package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DoubleShulker implements Listener {

	@EventHandler
	public void onShulkerKill(EntityDeathEvent e) {
		if(e.getEntity() instanceof Shulker) {
			e.getDrops().clear();
			e.getDrops().add(new ItemStack(Material.SHULKER_SHELL, 2));
		}
	}

}