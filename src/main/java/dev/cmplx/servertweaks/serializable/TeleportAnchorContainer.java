package dev.cmplx.servertweaks.serializable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Lectern;

public class TeleportAnchorContainer implements Serializable {

	public Map<UUID, TeleportAnchorInfo> anchors = new HashMap<>();

	public static class TeleportAnchorInfo implements Serializable {
		public int x, y, z;
		public UUID dim;
		public String name;

		public TeleportAnchorInfo(Lectern l) {
			var loc = l.getBlock().getLocation();
			x = loc.getBlockX();
			y = loc.getBlockY();
			z = loc.getBlockZ();
			dim = loc.getWorld().getUID();
			name = "Teleport Anchor";
		}

	}
}
