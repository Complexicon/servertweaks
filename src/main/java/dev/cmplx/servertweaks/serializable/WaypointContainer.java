package dev.cmplx.servertweaks.serializable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class WaypointContainer implements Serializable {

	public Map<UUID, WaypointInfo> waypoints = new HashMap<>();

	public static class WaypointInfo implements Serializable {
		public int x, y, z;
		public UUID dim;
		public String name;

		public WaypointInfo(Sign s) {
			x = s.getX();
			y = s.getY();
			z = s.getZ();
			dim = s.getWorld().getUID();
			name = s.getSide(Side.FRONT).getLine(0);
		}
	}

}
