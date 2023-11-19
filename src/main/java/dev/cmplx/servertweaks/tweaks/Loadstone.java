package dev.cmplx.servertweaks.tweaks;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class Loadstone implements Listener {
	
	@EventHandler
	void onLoadstonePlace(BlockBreakEvent e) {

		if (e.getBlock().getType() == Material.LODESTONE) {
			e.getPlayer().getWorld().setChunkForceLoaded(e.getBlock().getChunk().getX(), e.getBlock().getChunk().getZ(), false);
			e.getPlayer().sendMessage("§7Chunkloader Entfernt.");
		}

	}

	@EventHandler
	public void onLoadstonePlace(BlockPlaceEvent e) {

		if (e.getBlock().getType() == Material.LODESTONE) {
			e.getPlayer().getWorld().setChunkForceLoaded(e.getBlock().getChunk().getX(), e.getBlock().getChunk().getZ(), true);
			e.getPlayer().sendMessage("§aChunkloader Platziert.");
		}
	}

}