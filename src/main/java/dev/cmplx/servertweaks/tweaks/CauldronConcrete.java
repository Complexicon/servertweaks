package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CauldronConcrete implements Listener {

	@EventHandler
	public void onPlayerUseCauldron(PlayerInteractEvent e) {

		if(e.getClickedBlock() == null) return;

		String materialKey = e.getMaterial().getKey().getKey();

		if(e.getClickedBlock().getType() == Material.WATER_CAULDRON && materialKey.endsWith("concrete_powder")) {
			e.setCancelled(true);
			
			String newKey = materialKey.substring(0, materialKey.length() - 7); // cut off _powder (example: red_concrete_powder -> red_concrete)
			e.getItem().setType(Material.matchMaterial(newKey)); // find material by name
			e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_PLAYER_SWIM, SoundCategory.BLOCKS, 0.8f, 1.2f);
		}
	}

}
