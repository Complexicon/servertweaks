package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Fireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import dev.cmplx.servertweaks.Config;

public class MobGriefing implements Listener {

	@EventHandler
	public void onCreeperOrGhastExplode(EntityExplodeEvent e) {
		if(!Config.blockMobExplosion) return;

		if(e.getEntity() instanceof Creeper || e.getEntity() instanceof Fireball) {
			e.setYield(0);
			e.blockList().clear();
		}
	}

	@EventHandler
	public void onEnderGrief(EntityChangeBlockEvent e) {
		if(!Config.blockEnderGrief) return;

		if(e.getEntity() instanceof Enderman && e.getTo() == Material.AIR) {
			e.setCancelled(true);
		}
	}
	
}
