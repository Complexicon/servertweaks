package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickHarvest implements Listener {

	@EventHandler
	public void handleInteract(PlayerInteractEvent e) {

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!(e.getClickedBlock().getBlockData() instanceof Ageable)) return;

		Ageable crop = (Ageable) e.getClickedBlock().getBlockData();
		if (crop.getAge() != crop.getMaximumAge()) return;

		Material curType = e.getClickedBlock().getType();

		e.getClickedBlock().breakNaturally();
		e.getClickedBlock().setType(curType);
		crop.setAge(0);
		e.getClickedBlock().setBlockData(crop);
	}
	
}
