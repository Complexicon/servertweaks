package dev.cmplx.servertweaks.tweaks;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

import dev.cmplx.servertweaks.Main;

public class FastLeafDecay implements Listener {

	static final List<BlockFace> NEIGHBORS = Arrays.asList(BlockFace.UP, BlockFace.NORTH, BlockFace.EAST,
			BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN);

	@EventHandler
	void onLeafDecay(LeavesDecayEvent e) {
		for (var neighborFace : NEIGHBORS) {
			var neighbor = e.getBlock().getRelative(neighborFace);
			if (!Tag.LEAVES.isTagged(neighbor.getType())) continue;
			Bukkit.getScheduler().runTaskLater(Main.pluginRef, () -> tryDecay(neighbor), 5);
		}
	}

	void tryDecay(Block block) {
		if (!Tag.LEAVES.isTagged(block.getType())) return; // check again because it may already be decayed
        Leaves leaves = (Leaves) block.getBlockData();
		if (leaves.isPersistent() || leaves.getDistance() < 7) return;
		var e = new LeavesDecayEvent(block);
		Bukkit.getPluginManager().callEvent(e);
		if (e.isCancelled()) return;
		block.breakNaturally();
	}

}
