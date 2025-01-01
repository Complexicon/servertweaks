package dev.cmplx.servertweaks.tweaks.blocks;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

public class DualDoor  implements Listener {

	@EventHandler
	public void onDoorOpen(PlayerInteractEvent e) {

 		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		if (!(e.getClickedBlock().getBlockData() instanceof Door d)) return;

		Location l = e.getClickedBlock().getLocation();
	
		Arrays.asList(
			new Vector(1, 0, 0),
			new Vector(-1, 0, 0),
			new Vector(0, 0, 1),
			new Vector(0, 0, -1)
		)
		.stream()
		.map(vec -> l.getWorld().getBlockAt(l.clone().add(vec)))
		.filter(block -> 
			(block.getBlockData() instanceof Door) &&						// is door
			(((Door)block.getBlockData()).isOpen() == d.isOpen()) &&		// with same open state
			(((Door)block.getBlockData()).getFacing() == d.getFacing()) &&  // facing same direction
			(((Door)block.getBlockData()).getHinge() != d.getHinge()) 		// and hinged on opposite site
		)
		.findFirst()
		.ifPresent(doorBlock -> {
			var doorData = (Door) doorBlock.getBlockData();
			doorData.setOpen(!d.isOpen());
			doorBlock.setBlockData(doorData);
		});
	}
}
