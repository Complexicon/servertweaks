package dev.cmplx.servertweaks.tweaks;

import java.util.Map;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import dev.cmplx.servertweaks.i18n;

public class DeathCoords implements Listener {

	final Map<String, String> worldLookup = Map.of(
		"world", "Overworld",
		"world_nether", "Nether",
		"world_the_end", "End"
	);

	@EventHandler
	void onDeath(PlayerDeathEvent e) {
		var deathCoords = e.getEntity().getLocation();
		
		var world = worldLookup.getOrDefault(deathCoords.getWorld().getName(), deathCoords.getWorld().getName());

		e.getEntity().sendMessage(i18n.DEATH_COORDS.fmt(
			i18n.param("x", deathCoords.getBlockX()),
			i18n.param("y", deathCoords.getBlockY()),
			i18n.param("z", deathCoords.getBlockZ()),
			i18n.param("world", world)
		));
	}

}
