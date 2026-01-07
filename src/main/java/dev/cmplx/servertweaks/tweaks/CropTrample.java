package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class CropTrample implements Listener {
	
	@EventHandler
	void onPlayerTrample(PlayerInteractEvent e) {
		if (e.getAction() != Action.PHYSICAL) return;
		if (e.getClickedBlock().getType() != Material.FARMLAND) return;
		e.setCancelled(true);
	}

}
